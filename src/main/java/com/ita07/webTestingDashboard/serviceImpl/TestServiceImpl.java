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
import jakarta.annotation.PostConstruct;
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
import java.util.concurrent.atomic.AtomicLong;

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
    // Track active test runs with their unique IDs and Future objects
    private static final Map<Integer, Future<?>> activeTestRuns = new ConcurrentHashMap<>();

    public static boolean isTestFutureActive(int testRunId) {
        return activeTestRuns.containsKey(testRunId);
    }

    // Use an incrementing counter for test run IDs, reset when all runs are finished
    private static final AtomicInteger testRunIdCounter = new AtomicInteger(1);

    // Store the last assigned testRunId for controller access
    private static final ThreadLocal<Integer> lastAssignedTestRunId = new ThreadLocal<>();
    public static int getCurrentTestRunId() {
        Integer id = lastAssignedTestRunId.get();
        return id != null ? id : -1;
    }

    // Store results for each testRunId
    private static final ConcurrentHashMap<Integer, List<ActionResult>> testResults = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Integer, TestRequest> testRequests = new ConcurrentHashMap<>();

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
        // Cancel all active test runs before shutting down
        activeTestRuns.forEach((id, future) -> future.cancel(true));
        executorService.shutdown();
    }

    // testRunIdGenerator is initialized with new AtomicLong(1).
    // The first call to testRunIdGenerator.getAndIncrement() will return 1, making the first testRunId = 1.
    // If the first observed ID is 2 on a clean application start, it implies an additional increment occurred
    // before this method's invocation in that specific run, potentially due to environment or other startup processes.
    @Override
    public List<ActionResult> executeActions(TestRequest request) {
        // Assign an incrementing test run ID
        int testRunId = testRunIdCounter.getAndIncrement();
        lastAssignedTestRunId.set(testRunId);
        logger.info("Starting test run {} with {} actions", testRunId,
                request.getActions() != null ? request.getActions().size() : 0);

        queuedCount.incrementAndGet();
        Future<List<ActionResult>> future = null; // Declared to be in scope for activeTestRuns.put

        try {
            final int finalTestRunId = testRunId;
            future = executorService.submit(() -> {
                try {
                    queuedCount.decrementAndGet(); // Decrement when actual execution begins
                    logger.info("Executing test run {}", finalTestRunId);
                    return executeActionsInternal(request, testRunId); // Pass testRunId to executeActionsInternal
                } catch (Exception e) {
                    logger.error("Error during execution of test run {}: {}", finalTestRunId, e.getMessage(), e);
                    throw e; // Propagate to be caught by ExecutionException in the outer try-catch
                }
            });

            // Track this test run's Future object in the activeTestRuns map.
            // This allows it to be cancelled later if needed.
            activeTestRuns.put(testRunId, future);

            try {
                // Wait for the test run to complete and get its results.
                List<ActionResult> results = future.get();
                logger.info("Test run {} completed successfully.", testRunId);
                return results;
            } catch (CancellationException e) {
                logger.info("Test run {} was cancelled.", testRunId);
                // Construct and return a result indicating cancellation.
                List<ActionResult> cancelResults = new ArrayList<>();
                cancelResults.add(new ActionResult("cancelled", "cancelled", "Test run " + testRunId + " was cancelled.", null, 0, "Test run ID: " + testRunId));
                return cancelResults;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupted status.
                logger.error("Test run {} was interrupted.", testRunId, e);
                throw new RuntimeException("Test run " + testRunId + " was interrupted.", e);
            } catch (ExecutionException e) {
                logger.error("Test run {} execution failed with an underlying cause.", testRunId, e.getCause());
                throw new RuntimeException("Test run " + testRunId + " execution failed: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()), e.getCause() != null ? e.getCause() : e);
            }
        } finally {
            // Ensure the test run is removed from the activeTestRuns map
            if (activeTestRuns.containsKey(testRunId)) {
                activeTestRuns.remove(testRunId);
                logger.debug("Removed test run {} from activeTestRuns map in finally block of executeActions.", testRunId);
            }
            // Reset the counter only if no active test runs remain
            if (activeTestRuns.isEmpty()) {
                testRunIdCounter.set(1);
                logger.info("All test runs finished. Resetting testRunIdCounter to 1.");
            }
        }
    }

    @Override
    public boolean cancelTestRun(long testRunId) {
        logger.info("Attempting to cancel test run {}.", testRunId);
        Future<?> future = activeTestRuns.get((int)testRunId);

        if (future == null) {
            logger.warn("Test run {} not found in activeTestRuns. It might have already completed or been cancelled.", testRunId);
            return false;
        }

        boolean cancelled = future.cancel(true);

        if (cancelled) {
            logger.info("Test run {} successfully cancelled via future.cancel(true).", testRunId);
            activeTestRuns.remove((int)testRunId);

            TestRequest testRequest = testRequests.get((int)testRunId); // Retrieve the TestRequest

            // Save the canceled test run to the database
            try {
                TestRun testRun = new TestRun();
                testRun.setBrowser(testRequest != null ? testRequest.getBrowser() : "unknown");
                testRun.setExecutedAt(LocalDateTime.now());
                testRun.setActionsJson(testRequest != null ? objectMapper.writeValueAsString(testRequest.getActions()) : "[]");
                // Directly use the results from the testResults map
                testRun.setResultsJson("[]"); // No results to save since it was cancelled
                testRun.setStatus("cancelled");
                testRunRepository.save(testRun);
            } catch (Exception e) {
                logger.error("Failed to save canceled test run to the database", e);
            }
        } else {
            logger.warn("Failed to cancel test run {} via future.cancel(true). It may have already completed, been cancelled, or is non-interruptible.", testRunId);
        }
        testRequests.remove((int)testRunId); // Clean up TestRequest
        return cancelled;
    }

    public static int getQueuedCount() { // Ensure this method exists if it was part of previous thoughts, or adjust if queuedCount was removed
        return executorService.getQueue().size();
    }

    // The original logic moved to a new method
    private List<ActionResult> executeActionsInternal(TestRequest request, int testRunId) {
        validateTestRequest(request);
        String browser = request.getBrowser() != null ? request.getBrowser() : "chrome";
        WebDriver driver = SeleniumConfig.createDriver(browser);
        List<ActionResult> results = List.of();

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
            if (results == null) { // Ensure results is never null if method completes normally
                logger.warn("SeleniumActionExecutor.executeActions returned null. Defaulting to an empty list of results.");
                results = new ArrayList<>();
            }
        }
        finally {
            driver.quit();
        }

        // Save test run to DB
        try {
            TestRun testRun = new TestRun();
            testRun.setBrowser(browser);
            testRun.setExecutedAt(LocalDateTime.now());
            testRun.setActionsJson(objectMapper.writeValueAsString(request.getActions()));
            testRun.setResultsJson(objectMapper.writeValueAsString(results));
            testRun.setStatus("finished"); // Set status to finished
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
            // Suppressed unchecked cast warnings
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
                    // Suppressed unchecked cast warnings
                    @SuppressWarnings("unchecked")
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
                    // Suppressed unchecked cast warnings
                    @SuppressWarnings("unchecked")
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
                    // Suppressed unchecked cast warnings
                    @SuppressWarnings("unchecked")
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
                                // Suppressed unchecked cast warnings
                                @SuppressWarnings("unchecked")
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
                            // Suppressed unchecked cast warnings
                            @SuppressWarnings("unchecked")
                            Map<String, String> locator = (Map<String, String>) action.get("locator");
                            if (!locator.containsKey("type") || !locator.containsKey("value") || locator.get("type").isBlank() || locator.get("value").isBlank()) {
                                throw new ValidationException("'locator.type' and 'locator.value' are required for 'assert' with '" + condition + "' condition at index " + i + ".");
                            }
                        }
                        case "attributevalue" -> {
                            if (action.get("locator") == null || !(action.get("locator") instanceof Map)) {
                                throw new ValidationException("'locator' is required for 'assert' with 'attributeValue' condition at index " + i + ".");
                            }
                            // Suppressed unchecked cast warnings
                            @SuppressWarnings("unchecked")
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
                    // Suppressed unchecked cast warnings
                    @SuppressWarnings("unchecked")
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
                    // Suppressed unchecked cast warnings
                    @SuppressWarnings("unchecked")
                    Map<String, String> sourceLocator = (Map<String, String>) action.get("sourceLocator");
                    // Suppressed unchecked cast warnings
                    @SuppressWarnings("unchecked")
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

    public static List<ActionResult> getResultsForRun(int testRunId) {
        logger.debug("Fetching results for testRunId: {}", testRunId);
        List<ActionResult> results = testResults.get(testRunId);
        if (results == null) {
            logger.warn("No results found for testRunId: {}", testRunId);
        } else {
            logger.info("Results successfully fetched for testRunId: {}", testRunId);
        }
        return results;
    }

    // Submit test asynchronously and return testRunId immediately
    public int submitTestAsync(TestRequest request) {
        int testRunId = testRunIdCounter.getAndIncrement(); // Get ID
        testRequests.put(testRunId, request); // Store the TestRequest
        logger.info("Storing TestRequest in map for test run {}", testRunId);

        Future<?> future;
        try {
            future = executorService.submit(() -> {
                try {
                    List<ActionResult> resultsInternal = executeActionsInternal(request, testRunId);
                    logger.info("For testRunId {}: executeActionsInternal returned. Results null? {}", testRunId, resultsInternal == null);
                    testResults.put(testRunId, resultsInternal);
                    logger.info("For testRunId {}: Put results into map. Map size: {}", testRunId, testResults.size());
                } catch (Exception e) {
                    logger.error("For testRunId {}: Error in async test run: {}", testRunId, e.getMessage(), e);
                    testResults.put(testRunId, List.of());
                    logger.info("For testRunId {}: Put empty list into map due to error. Map size: {}", testRunId, testResults.size());
                } finally {
                    activeTestRuns.remove(testRunId);
                    testRequests.remove(testRunId); // Clean up TestRequest after completion
                    if (activeTestRuns.isEmpty() && executorService.getQueue().isEmpty()) {
                        testRunIdCounter.set(1);
                        logger.info("All active test runs finished and executor queue is empty. Resetting testRunIdCounter to 1.");
                    }
                }
            });
            activeTestRuns.put(testRunId, future);
        } catch (RejectedExecutionException ree) {
            testRequests.remove(testRunId); // Clean up TestRequest if execution is rejected
            logger.error("Test run {} rejected by executor service: {}", testRunId, ree.getMessage());
            throw new RuntimeException("Test execution queue is full. Please try again later.", ree);
        }
        return testRunId;
    }

    public static void clearPersistedTestResults(int testRunId) {
        if (testResults.containsKey(testRunId)) {
            testResults.remove(testRunId);
            logger.info("Cleared persisted results for test run ID: {}", testRunId);
        } else {
            logger.warn("Attempted to clear results for test run ID: {}, but they were not found.", testRunId);
        }
    }

    public boolean isTestRunActive(int testRunId) {
        TestRun testRun = testRunRepository.findById((long) testRunId).orElse(null);
        if (testRun == null) {
            return false; // Test run not found
        }
        return "running".equalsIgnoreCase(testRun.getStatus());
    }
}
