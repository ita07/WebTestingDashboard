package com.ita07.webTestingDashboard.serviceImpl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ita07.webTestingDashboard.exception.ValidationException;
import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestData;
import com.ita07.webTestingDashboard.model.TestRequest;
import com.ita07.webTestingDashboard.selenium.config.SeleniumConfig;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.service.TestDataService;
import com.ita07.webTestingDashboard.service.TestService;
import com.ita07.webTestingDashboard.model.TestRun;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.annotation.PreDestroy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;

@Service
public class TestServiceImpl implements TestService {
    private static final Logger logger = LoggerFactory.getLogger(TestServiceImpl.class);
    // Add a fixed thread pool for parallel test execution
    private static int MAX_PARALLEL_TESTS = 4; // Default, can be overridden by property
    // Expose the executor for status endpoint
    @Getter
    private static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(
            MAX_PARALLEL_TESTS, // corePoolSize
            MAX_PARALLEL_TESTS, // maximumPoolSize
            60L, // keepAliveTime
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(100) // Bounded queue, adjust capacity as needed
    );
    // Fast, non-blocking counter for queued test runs
    private static final AtomicInteger queuedCount = new AtomicInteger(0);

    @Autowired
    private TestRunRepository testRunRepository;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private TestDataService testDataService;
    @Value("${parallel.tests.max:4}")
    public void setMaxParallelTests(int max) {
        MAX_PARALLEL_TESTS = max;
        executorService.setCorePoolSize(max);
        executorService.setMaximumPoolSize(max);
    }
    private final ObjectMapper objectMapper = new ObjectMapper();

    @PreDestroy
    public void shutdownExecutor() {
        logger.info("Shutting down ExecutorService for parallel test execution...");
        executorService.shutdown();
    }

    @Override
    public List<ActionResult> executeActions(TestRequest request) {
        queuedCount.incrementAndGet();
        try {
            Future<List<ActionResult>> future = executorService.submit(() -> {
                queuedCount.decrementAndGet();
                try {
                    return executeActionsInternal(request);
                } catch (Exception e) {
                    logger.error("Exception in parallel test execution thread", e);
                    // Optionally, return a failed ActionResult for the whole test run
                    List<ActionResult> failed = new ArrayList<>();
                    failed.add(new ActionResult("testRun", "failure", "Test run failed: " + e.getMessage(), null, 0, "Exception: " + e.toString()));
                    return failed;
                }
            });
            return future.get(); // Wait for completion and return results
        } catch (Exception e) {
            logger.error("Parallel test execution failed", e);
            throw new RuntimeException("Parallel test execution failed: " + e.getMessage(), e);
        }
    }

    public static int getQueuedCount() {
        return queuedCount.get();
    }

    // The original logic moved to a new method
    private List<ActionResult> executeActionsInternal(TestRequest request) {
        validateTestRequest(request);
        String browser = request.getBrowser() != null ? request.getBrowser() : "chrome";
        WebDriver driver = SeleniumConfig.createDriver(browser);
        List<ActionResult> results;

        // Handle test data if provided
        List<Map<String, Object>> resolvedActions = request.getActions();
        if (request.getTestDataId() != null) {
            try {
                TestData testData = testDataService.getTestData(request.getTestDataId());
                // Properly parse the JSON string into a Map
                Map<String, Object> variables = objectMapper.readValue(testData.getDataJson(), new TypeReference<>() {});
                logger.info("Loaded test data variables: {}", variables);

                resolvedActions = request.getActions().stream()
                    .map(action -> {
                        Map<String, Object> resolvedAction = testDataService.resolveVariables(new HashMap<>(action), variables);
                        logger.info("Original action: {}, Resolved action: {}", action, resolvedAction);
                        return resolvedAction;
                    })
                    .toList();
            } catch (Exception e) {
                logger.error("Failed to process test data: {}", e.getMessage(), e);
                throw new ValidationException("Failed to process test data: " + e.getMessage());
            }
        }

        try {
            SeleniumActionExecutor executor = new SeleniumActionExecutor(driver);
            results = executor.executeActions(resolvedActions, request.isStopOnFailure());
        } finally {
            driver.quit();
        }

        // Save test run to DB
        try {
            TestRun testRun = new TestRun();
            testRun.setBrowser(browser);
            testRun.setExecutedAt(LocalDateTime.now());
            testRun.setActionsJson(objectMapper.writeValueAsString(request.getActions()));
            testRun.setResultsJson(objectMapper.writeValueAsString(results));
            testRunRepository.save(testRun);
        } catch (Exception e) {
            logger.error("Failed to save test run to the database", e);
        }

        // Generate and save HTML report
        String htmlContent = generateHtmlReport(results);
        String reportPath = saveHtmlReport(htmlContent);
        if (reportPath != null) {
            logger.info("Test report saved at: {}", reportPath);
        }

        return results;
    }

    private void validateTestRequest(TestRequest request) {
        if (request == null) {
            throw new ValidationException("Request body is missing.");
        }
        if (request.getBrowser() != null) {
            String browser = request.getBrowser().toLowerCase();
            if (!browser.equals("chrome") && !browser.equals("firefox") && !browser.equals("edge")) {
                throw new ValidationException("Unsupported browser type: " + request.getBrowser());
            }
        }
        if (request.getActions() == null || request.getActions().isEmpty()) {
            throw new ValidationException("Actions list is missing or empty.");
        }
        int i = 0;
        for (Object actionObj : request.getActions()) {
            if (!(actionObj instanceof Map)) {
                throw new ValidationException("Action at index " + i + " is not a valid object.");
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> action = (Map<String, Object>) actionObj;
            String type = (String) action.get("action");
            if (type == null || type.isBlank()) {
                throw new ValidationException("Action type is missing at index " + i + ".");
            }
            // List of supported actions
            final List<String> supportedActions = List.of(
                "navigate", "click", "type", "wait", "select", "check", "uncheck", "hover", "scroll", "upload", "assert", "doubleclick", "draganddrop"
            );
            if (!supportedActions.contains(type.toLowerCase())) {
                throw new ValidationException("Unsupported action: '" + type + "' at index " + i + ".");
            }
            switch (type.toLowerCase()) {
                case "navigate" -> {
                    if (action.get("url") == null || ((String) action.get("url")).isBlank()) {
                        throw new ValidationException("'url' is required for 'navigate' action at index " + i + ".");
                    }
                }
                case "click", "type" -> {
                    if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                        throw new ValidationException("'locator' is required for '" + type + "' action at index " + i + ".");
                    }
                    Map<String, String> locator = (Map<String, String>) action.get("locator");
                    if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                        throw new ValidationException("'locator.type' and 'locator.value' are required for '" + type + "' action at index " + i + ".");
                    }
                    String locatorType = locator.get("type").toLowerCase();
                    if (!locatorType.equals("id") && !locatorType.equals("name") && !locatorType.equals("xpath") && !locatorType.equals("cssselector") &&
                        !locatorType.equals("classname") && !locatorType.equals("tagname") && !locatorType.equals("linktext") && !locatorType.equals("partiallinktext")) {
                        throw new ValidationException("Unsupported locator type: '" + locatorType + "' for action '" + type + "' at index " + i + ".");
                    }
                    if (type.equalsIgnoreCase("type") && (action.get("text") == null || ((String) action.get("text")).isBlank())) {
                        throw new ValidationException("'text' is required for 'type' action at index " + i + ".");
                    }
                }
                case "wait" -> {
                    if (action.get("seconds") == null) {
                        throw new ValidationException("'seconds' is required for 'wait' action at index " + i + ".");
                    }
                    Object secondsObj = action.get("seconds");
                    int seconds;
                    try {
                        seconds = (secondsObj instanceof Number) ? ((Number) secondsObj).intValue() : Integer.parseInt(secondsObj.toString());
                    } catch (Exception e) {
                        throw new ValidationException("'seconds' must be a valid integer for 'wait' action at index " + i + ".");
                    }
                    if (seconds < 0) {
                        throw new ValidationException("'seconds' must be non-negative for 'wait' action at index " + i + ".");
                    }
                }
                case "select" -> {
                    if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                        throw new ValidationException("'locator' is required for 'select' action at index " + i + ".");
                    }
                    Map<String, String> locator = (Map<String, String>) action.get("locator");
                    if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                        throw new ValidationException("'locator.type' and 'locator.value' are required for 'select' action at index " + i + ".");
                    }
                    String locatorType = locator.get("type").toLowerCase();
                    if (!locatorType.equals("id") && !locatorType.equals("name") && !locatorType.equals("xpath") && !locatorType.equals("cssselector") &&
                        !locatorType.equals("classname") && !locatorType.equals("tagname") && !locatorType.equals("linktext") && !locatorType.equals("partiallinktext")) {
                        throw new ValidationException("Unsupported locator type: '" + locatorType + "' for action '" + type + "' at index " + i + ".");
                    }
                    if (action.get("selectBy") == null || ((String) action.get("selectBy")).isBlank()) {
                        throw new ValidationException("'selectBy' is required for 'select' action at index " + i + ".");
                    }
                    String selectBy = ((String) action.get("selectBy")).toLowerCase();
                    if (!selectBy.equals("value") && !selectBy.equals("visibletext") && !selectBy.equals("index")) {
                        throw new ValidationException("'selectBy' must be one of 'value', 'visibleText', or 'index' for 'select' action at index " + i + ".");
                    }
                    if (action.get("option") == null || ((String) action.get("option")).isBlank()) {
                        throw new ValidationException("'option' is required for 'select' action at index " + i + ".");
                    }
                }
                case "check", "uncheck" -> {
                    if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                        throw new ValidationException("'locator' is required for '" + type + "' action at index " + i + ".");
                    }
                    Map<String, String> locator = (Map<String, String>) action.get("locator");
                    if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                        throw new ValidationException("'locator.type' and 'locator.value' are required for '" + type + "' action at index " + i + ".");
                    }
                    String locatorType = locator.get("type").toLowerCase();
                    if (!locatorType.equals("id") && !locatorType.equals("name") && !locatorType.equals("xpath") && !locatorType.equals("cssselector") &&
                        !locatorType.equals("classname") && !locatorType.equals("tagname") && !locatorType.equals("linktext") && !locatorType.equals("partiallinktext")) {
                        throw new ValidationException("Unsupported locator type: '" + locatorType + "' for action '" + type + "' at index " + i + ".");
                    }
                }
                case "assert" -> {
                    if (action.get("condition") == null || ((String) action.get("condition")).isBlank()) {
                        throw new ValidationException("'condition' is required for 'assert' action at index " + i + ".");
                    }
                    String condition = ((String) action.get("condition")).toLowerCase();
                    switch (condition) {
                        case "text", "title" -> {
                            if (condition.equals("text")) {
                                if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                                    throw new ValidationException("'locator' is required for 'assert' with 'text' condition at index " + i + ".");
                                }
                                Map<String, String> locator = (Map<String, String>) action.get("locator");
                                if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                                    throw new ValidationException("'locator.type' and 'locator.value' are required for 'assert' with 'text' condition at index " + i + ".");
                                }
                            }
                            if (action.get("expected") == null || ((String) action.get("expected")).isBlank()) {
                                throw new ValidationException("'expected' is required for 'assert' with '" + condition + "' condition at index " + i + ".");
                            }
                        }
                        case "elementpresent", "elementvisible", "elementenabled" -> {
                            if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                                throw new ValidationException("'locator' is required for 'assert' with '" + condition + "' condition at index " + i + ".");
                            }
                            Map<String, String> locator = (Map<String, String>) action.get("locator");
                            if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                                throw new ValidationException("'locator.type' and 'locator.value' are required for 'assert' with '" + condition + "' condition at index " + i + ".");
                            }
                        }
                        case "attributevalue" -> {
                            if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                                throw new ValidationException("'locator' is required for 'assert' with 'attributeValue' condition at index " + i + ".");
                            }
                            Map<String, String> locator = (Map<String, String>) action.get("locator");
                            if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                                throw new ValidationException("'locator.type' and 'locator.value' are required for 'assert' with 'attributeValue' condition at index " + i + ".");
                            }
                            if (action.get("attribute") == null || ((String) action.get("attribute")).isBlank()) {
                                throw new ValidationException("'attribute' is required for 'assert' with 'attributeValue' condition at index " + i + ".");
                            }
                            if (action.get("expected") == null || ((String) action.get("expected")).isBlank()) {
                                throw new ValidationException("'expected' is required for 'assert' with 'attributeValue' condition at index " + i + ".");
                            }
                        }
                        default -> throw new ValidationException("Unsupported assertion condition: '" + condition + "' at index " + i + ".");
                    }
                }
                case "doubleclick" -> {
                    if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                        throw new ValidationException("'locator' is required for 'doubleclick' action at index " + i + ".");
                    }
                    Map<String, String> locator = (Map<String, String>) action.get("locator");
                    if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                        throw new ValidationException("'locator.type' and 'locator.value' are required for 'doubleclick' action at index " + i + ".");
                    }
                    String locatorType = locator.get("type").toLowerCase();
                    if (!locatorType.equals("id") && !locatorType.equals("name") && !locatorType.equals("xpath") && !locatorType.equals("cssselector") &&
                        !locatorType.equals("classname") && !locatorType.equals("tagname") && !locatorType.equals("linktext") && !locatorType.equals("partiallinktext")) {
                        throw new ValidationException("Unsupported locator type: '" + locatorType + "' for action 'doubleclick' at index " + i + ".");
                    }
                }
                case "draganddrop" -> {
                    if (action.get("sourceLocator") == null || !(action.get("sourceLocator") instanceof Map)) {
                        throw new ValidationException("'sourceLocator' is required for 'draganddrop' action at index " + i + ".");
                    }
                    if (action.get("targetLocator") == null || !(action.get("targetLocator") instanceof Map)) {
                        throw new ValidationException("'targetLocator' is required for 'draganddrop' action at index " + i + ".");
                    }
                    Map<String, String> sourceLocator = (Map<String, String>) action.get("sourceLocator");
                    Map<String, String> targetLocator = (Map<String, String>) action.get("targetLocator");
                    if (!sourceLocator.containsKey("type") || !sourceLocator.containsKey("value") || sourceLocator.get("type").isBlank() || sourceLocator.get("value").isBlank()) {
                        throw new ValidationException("'sourceLocator.type' and 'sourceLocator.value' are required for 'draganddrop' action at index " + i + ".");
                    }
                    if (!targetLocator.containsKey("type") || !targetLocator.containsKey("value") || targetLocator.get("type").isBlank() || targetLocator.get("value").isBlank()) {
                        throw new ValidationException("'targetLocator.type' and 'targetLocator.value' are required for 'draganddrop' action at index " + i + ".");
                    }
                    String sourceLocatorType = sourceLocator.get("type").toLowerCase();
                    String targetLocatorType = targetLocator.get("type").toLowerCase();
                    if (!sourceLocatorType.equals("id") && !sourceLocatorType.equals("name") && !sourceLocatorType.equals("xpath") && !sourceLocatorType.equals("cssselector") &&
                        !sourceLocatorType.equals("classname") && !sourceLocatorType.equals("tagname") && !sourceLocatorType.equals("linktext") && !sourceLocatorType.equals("partiallinktext")) {
                        throw new ValidationException("Unsupported sourceLocator type: '" + sourceLocatorType + "' for action 'draganddrop' at index " + i + ".");
                    }
                    if (!targetLocatorType.equals("id") && !targetLocatorType.equals("name") && !targetLocatorType.equals("xpath") && !targetLocatorType.equals("cssselector") &&
                        !targetLocatorType.equals("classname") && !targetLocatorType.equals("tagname") && !targetLocatorType.equals("linktext") && !targetLocatorType.equals("partiallinktext")) {
                        throw new ValidationException("Unsupported targetLocator type: '" + targetLocatorType + "' for action 'draganddrop' at index " + i + ".");
                    }
                }
            }
            i++;
        }
    }

    private String generateHtmlReport(List<ActionResult> results) {
        Context context = new Context();
        context.setVariable("results", results);
        context.setVariable("timestamp", LocalDateTime.now());
        double totalExecutionTimeSeconds = results.stream().mapToLong(ActionResult::getExecutionTimeMillis).sum() / 1000.0;
        context.setVariable("totalExecutionTimeSeconds", totalExecutionTimeSeconds);
        return templateEngine.process("report", context);
    }

    private String saveHtmlReport(String htmlContent) {
        String reportsDir = "reports";
        File dir = new File(reportsDir);
        if (!dir.exists() && !dir.mkdirs()) {
            logger.error("Failed to create directory: {}", reportsDir);
            return null;
        }

        String reportPath = reportsDir + "/test-report-" + System.currentTimeMillis() + ".html";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(reportPath))) {
            writer.write(htmlContent);
            return reportPath;
        } catch (IOException e) {
            logger.error("Failed to save HTML report", e);
            return null;
        }
    }
}


