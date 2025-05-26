package com.ita07.webTestingDashboard.serviceImpl;

import com.ita07.webTestingDashboard.exception.ValidationException;
import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestRequest;
import com.ita07.webTestingDashboard.selenium.config.SeleniumConfig;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.service.TestService;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TestServiceImpl implements TestService {

    @Override
    public List<ActionResult> executeActions(TestRequest request) {
        validateTestRequest(request);
        String browser = request.getBrowser() != null ? request.getBrowser() : "chrome";
        Map<String, Object> browserOptions = request.getBrowserOptions();
        WebDriver driver = SeleniumConfig.createDriver(browser, browserOptions);
        try {
            SeleniumActionExecutor executor = new SeleniumActionExecutor(driver);
            return executor.executeActions(request.getActions());
        } finally {
            driver.quit();
        }
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
                // Add more cases for other actions as needed
                default -> {
                    // Optionally, throw for unsupported actions
                }
            }
            i++;
        }
    }
}
