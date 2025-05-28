package com.ita07.webTestingDashboard.selenium.utils;

import com.ita07.webTestingDashboard.model.ActionResult;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class SeleniumActionExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumActionExecutor.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final WebDriver driver;
    private static final int DEFAULT_TIMEOUT = 10;

    public SeleniumActionExecutor(WebDriver driver) {
        this.driver = driver;
    }

    public List<ActionResult> executeActions(List<Map<String, Object>> actions, boolean stopOnFailure) {
        List<ActionResult> results = new ArrayList<>();
        boolean failed = false;
        for (Map<String, Object> action : actions) {
            String actionType = getActionType(action);
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            long startTime = System.currentTimeMillis();
            long endTime;
            long durationMillis;
            String details = buildDetailsString(actionType, action);

            if (failed) {
                logger.info("[{}] Skipping action '{}' due to previous failure.", timestamp, actionType);
                durationMillis = 0; // Skipped actions have 0 duration
                results.add(new ActionResult(actionType, "skipped", "Action was not executed due to previous failure.", null, durationMillis, details));
                continue;
            }
            try {
                logger.info("[{}] Starting action: {} with parameters: {}", timestamp, actionType, action);
                switch (actionType) {
                    case "navigate":
                        executeNavigateAction(action);
                        break;
                    case "click":
                        executeClickAction(action);
                        break;
                    case "type":
                        executeTypeAction(action);
                        break;
                    case "wait":
                        executeWaitAction(action);
                        break;
                    case "hover":
                        executeHoverAction(action);
                        break;
                    case "scroll":
                        executeScrollAction(action);
                        break;
                    case "upload":
                        executeUploadAction(action);
                        break;
                    case "assert":
                        executeAssertAction(action);
                        break;
                    case "select":
                        executeSelectAction(action);
                        break;
                    case "check":
                        executeCheckAction(action);
                        break;
                    case "uncheck":
                        executeUncheckAction(action);
                        break;
                    case "doubleclick":
                        executeDoubleClickAction(action);
                        break;
                    case "draganddrop":
                        executeDragAndDropAction(action);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported action: " + actionType);
                }
                endTime = System.currentTimeMillis();
                durationMillis = endTime - startTime;
                logger.info("[{}] Action '{}' executed successfully in {} ms.", timestamp, actionType, durationMillis);
                results.add(new ActionResult(actionType, "success", "Action executed successfully.", null, durationMillis, details));
            } catch (Exception e) {
                endTime = System.currentTimeMillis();
                durationMillis = endTime - startTime;
                String failMessage = extractErrorMessage(e);
                logger.error("[{}] Action '{}' failed after {} ms with error: {}", timestamp, actionType, durationMillis, failMessage, e);
                String screenshotPath = captureScreenshot(actionType);
                results.add(new ActionResult(actionType, "failure", failMessage, screenshotPath, durationMillis, details));
                if (stopOnFailure) {
                    failed = true;
                }
            }
        }
        return results;
    }

    private String captureScreenshot(String actionType) {
        try {
            String screenshotsDir = "screenshots";
            Files.createDirectories(Paths.get(screenshotsDir));
            String filename = actionType + "_" + UUID.randomUUID() + ".png";
            String filePath = screenshotsDir + "/" + filename;
            String base64 = SeleniumUtils.takeScreenshotAsBase64(driver);
            if (base64 != null) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
                    fos.write(imageBytes);
                }
                logger.info("Screenshot saved at: {}", filePath);
                // Return the path with leading forward slash for proper URL mapping
                return "/" + filePath;
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot for action '{}': {}", actionType, e.getMessage(), e);
        }
        return null;
    }

    // Keep the original method for backward compatibility
    public List<ActionResult> executeActions(List<Map<String, Object>> actions) {
        return executeActions(actions, false);
    }

    private String getActionType(Map<String, Object> action) {
        return ((String) action.get("action")).toLowerCase();
    }

    private void executeNavigateAction(Map<String, Object> action) {
        String url = (String) action.get("url");
        SeleniumUtils.navigateTo(driver, url);
    }

    private void executeClickAction(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        By by = getLocator(locator);
        WebElement clickableElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.clickElement(clickableElement);
    }

    private void executeTypeAction(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        String text = (String) action.get("text");
        By by = getLocator(locator);
        WebElement inputElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.enterText(inputElement, text);
    }

    private void executeWaitAction(Map<String, Object> action) {
        Object secondsObj = action.get("seconds");
        int seconds = (secondsObj instanceof Number) ? ((Number) secondsObj).intValue() : Integer.parseInt(secondsObj.toString());
        SeleniumUtils.waitSeconds(seconds);
    }

    private void executeHoverAction(Map<String, Object> action) {
        Map<String, String> hoverLocator = getLocatorFromAction(action);
        By by = getLocator(hoverLocator);
        WebElement hoverElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.hoverOverElement(driver, hoverElement);
    }

    private void executeScrollAction(Map<String, Object> action) {
        Map<String, String> scrollLocator = getLocatorFromAction(action);
        // Scroll to an element
        By by = getLocator(scrollLocator);
        WebElement scrollElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.scrollToElement(driver, scrollElement);
    }

    private void executeUploadAction(Map<String, Object> action) {
        Map<String, String> uploadLocator = getLocatorFromAction(action);
        String filePath = (String) action.get("filePath");
        By by = getLocator(uploadLocator);
        WebElement uploadElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.uploadFile(uploadElement, filePath);
    }

    private void executeAssertAction(Map<String, Object> action) {
        String condition = ((String) action.get("condition")).toLowerCase();
        switch (condition) {
            case "text":
                executeTextAssertion(action);
                break;
            case "title":
                executeTitleAssertion(action);
                break;
            case "elementpresent":
                executeElementPresentAssertion(action);
                break;
            case "elementvisible":
                executeElementVisibleAssertion(action);
                break;
            case "elementenabled":
                executeElementEnabledAssertion(action);
                break;
            case "attributevalue":
                executeAttributeValueAssertion(action);
                break;
            default:
                throw new IllegalArgumentException("Unsupported assertion condition: " + condition);
        }
    }

    private void executeTextAssertion(Map<String, Object> action) {
        Map<String, String> textLocator = getLocatorFromAction(action);
        String expectedText = (String) action.get("expected");
        By by = getLocator(textLocator);
        WebElement element = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.assertTextEquals(element, expectedText);
    }

    private void executeTitleAssertion(Map<String, Object> action) {
        String expectedTitle = (String) action.get("expected");
        SeleniumUtils.assertPageTitleEquals(driver, expectedTitle);
    }

    private void executeElementPresentAssertion(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        By by = getLocator(locator);
        SeleniumUtils.assertElementPresent(driver, by);
    }

    private void executeElementVisibleAssertion(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        By by = getLocator(locator);
        SeleniumUtils.assertElementVisible(driver, by);
    }

    private void executeElementEnabledAssertion(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        By by = getLocator(locator);
        SeleniumUtils.assertElementEnabled(driver, by);
    }

    private void executeAttributeValueAssertion(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        String attribute = (String) action.get("attribute");
        String expected = (String) action.get("expected");
        By by = getLocator(locator);
        WebElement element = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.assertAttributeValue(element, attribute, expected);
    }

    private void executeSelectAction(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        String selectBy = ((String) action.get("selectBy")).toLowerCase();
        String option = (String) action.get("option");
        By by = getLocator(locator);
        WebElement selectElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.selectDropdownOption(selectElement, selectBy, option);
    }

    private void executeCheckAction(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        By by = getLocator(locator);
        WebElement element = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.checkElement(element);
    }

    private void executeUncheckAction(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        By by = getLocator(locator);
        WebElement element = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.uncheckElement(element);
    }

    private void executeDoubleClickAction(Map<String, Object> action) {
        Map<String, String> locator = getLocatorFromAction(action);
        By by = getLocator(locator);
        WebElement element = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.doubleClickElement(driver, element);
    }

    @SuppressWarnings("unchecked")
    private void executeDragAndDropAction(Map<String, Object> action) {
        Map<String, String> sourceLocator = (Map<String, String>) action.get("sourceLocator");
        Map<String, String> targetLocator = (Map<String, String>) action.get("targetLocator");
        By sourceBy = getLocator(sourceLocator);
        By targetBy = getLocator(targetLocator);
        WebElement sourceElement = SeleniumUtils.waitForElementVisible(driver, sourceBy, DEFAULT_TIMEOUT);
        WebElement targetElement = SeleniumUtils.waitForElementVisible(driver, targetBy, DEFAULT_TIMEOUT);
        SeleniumUtils.dragAndDropElement(driver, sourceElement, targetElement);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> getLocatorFromAction(Map<String, Object> action) {
        // Validation is already handled in the service layer
        return (Map<String, String>) action.get("locator");
    }

    private By getLocator(Map<String, String> locator) {
        String type = locator.get("type");
        String value = locator.get("value");

        return switch (type.toLowerCase()) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "xpath" -> By.xpath(value);
            case "cssselector" -> By.cssSelector(value);
            case "classname" -> By.className(value);
            case "tagname" -> By.tagName(value);
            case "linktext" -> By.linkText(value);
            case "partiallinktext" -> By.partialLinkText(value);
            default -> throw new IllegalArgumentException("Unsupported locator type: " + type);
        };
    }

    private String extractErrorMessage(Exception e) {
        String fullMessage = e.getMessage();
        if (fullMessage != null) {
            int newlineIndex = fullMessage.indexOf("\n");
            return newlineIndex != -1 ? fullMessage.substring(0, newlineIndex) : fullMessage;
        }
        return "An unknown error occurred.";
    }

    private String buildDetailsString(String actionType, Map<String, Object> action) {
        StringBuilder sb = new StringBuilder();
        switch (actionType) {
            case "navigate" -> sb.append("url=").append(action.getOrDefault("url", ""));
            case "click", "check", "uncheck", "hover", "scroll", "doubleclick" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
            }
            case "type" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
                sb.append(", text=").append(action.getOrDefault("text", ""));
            }
            case "wait" -> sb.append("seconds=").append(action.getOrDefault("seconds", ""));
            case "select" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
                sb.append(", selectBy=").append(action.getOrDefault("selectBy", ""));
                sb.append(", option=").append(action.getOrDefault("option", ""));
            }
            case "upload" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
                sb.append(", filePath=").append(action.getOrDefault("filePath", ""));
            }
            case "assert" -> {
                sb.append("condition=").append(action.getOrDefault("condition", ""));
                if (action.get("locator") != null) sb.append(", locator=").append(stringifyLocator(action.get("locator")));
                if (action.get("expected") != null) sb.append(", expected=").append(action.get("expected"));
                if (action.get("attribute") != null) sb.append(", attribute=").append(action.get("attribute"));
            }
            case "draganddrop" -> {
                sb.append("sourceLocator=").append(stringifyLocator(action.get("sourceLocator")));
                sb.append(", targetLocator=").append(stringifyLocator(action.get("targetLocator")));
            }
        }
        return sb.toString();
    }

    private String stringifyLocator(Object locatorObj) {
        if (locatorObj instanceof Map<?, ?> loc) {
            Object typeObj = loc.get("type");
            Object valueObj = loc.get("value");

            String type = (typeObj != null) ? String.valueOf(typeObj) : "";
            String value = (valueObj != null) ? String.valueOf(valueObj) : "";

            return "{" + type + ": '" + value + "'}";
        }
        return String.valueOf(locatorObj);
    }
}
