package com.ita07.webTestingDashboard.selenium.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import com.ita07.webTestingDashboard.model.ActionResult;
import java.util.ArrayList;

import java.util.List;
import java.util.Map;

public class SeleniumActionExecutor {
    private final WebDriver driver;
    private static final int DEFAULT_TIMEOUT = 10;

    public SeleniumActionExecutor(WebDriver driver) {
        this.driver = driver;
    }

    public List<ActionResult> executeActions(List<Map<String, Object>> actions) {
        List<ActionResult> results = new ArrayList<>();
        for (Map<String, Object> action : actions) {
            String actionType = getActionType(action);
            try {
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
                results.add(new ActionResult(actionType, "success", "Action executed successfully."));
            } catch (Exception e) {
                String screenshotBase64 = SeleniumUtils.takeScreenshotAsBase64(driver);
                results.add(new ActionResult(actionType, "failure", e.getMessage(), screenshotBase64));
            }
        }
        return results;
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
}

