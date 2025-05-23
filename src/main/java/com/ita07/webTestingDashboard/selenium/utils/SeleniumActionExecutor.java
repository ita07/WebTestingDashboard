package com.ita07.webTestingDashboard.selenium.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;
import java.util.Map;

public class SeleniumActionExecutor {
    private final WebDriver driver;
    private static final int DEFAULT_TIMEOUT = 10;

    public SeleniumActionExecutor(WebDriver driver) {
        this.driver = driver;
    }

    public void executeActions(List<Map<String, Object>> actions) {
        for (Map<String, Object> action : actions) {
            String actionType = getActionType(action);
            
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
                default:
                    throw new IllegalArgumentException("Unsupported action: " + actionType);
            }
        }
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

    private void executeHoverAction(Map<String, Object> action) {
        Map<String, String> hoverLocator = getLocatorFromAction(action);
        By by = getLocator(hoverLocator);
        WebElement hoverElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.hoverOverElement(driver, hoverElement);
    }

    private void executeScrollAction(Map<String, Object> action) {
        Map<String, String> scrollLocator = (Map<String, String>) action.get("locator");
        if (scrollLocator != null) {
            // Scroll to an element
            By by = getLocator(scrollLocator);
            WebElement scrollElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
            SeleniumUtils.scrollToElement(driver, scrollElement);
        } else {
            // Scroll to a specific position
            int x = (int) action.getOrDefault("x", 0);
            int y = (int) action.getOrDefault("y", 0);
            SeleniumUtils.scrollToPosition(driver, x, y);
        }
    }

    private void executeUploadAction(Map<String, Object> action) {
        Map<String, String> uploadLocator = getLocatorFromAction(action);
        String filePath = (String) action.get("filePath");
        By by = getLocator(uploadLocator);
        WebElement uploadElement = SeleniumUtils.waitForElementVisible(driver, by, DEFAULT_TIMEOUT);
        SeleniumUtils.uploadFile(uploadElement, filePath);
    }

    private void executeAssertAction(Map<String, Object> action) {
        String condition = (String) action.get("condition");
        switch (condition.toLowerCase()) {
            case "text":
                executeTextAssertion(action);
                break;
            case "title":
                executeTitleAssertion(action);
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

    private Map<String, String> getLocatorFromAction(Map<String, Object> action) {
        Object locatorObj = action.get("locator");

        if (locatorObj == null) {
            throw new IllegalArgumentException("Action is missing required 'locator' field");
        }

        if (!(locatorObj instanceof Map)) {
            throw new IllegalArgumentException("The 'locator' field must be a Map but was: " + locatorObj.getClass().getName());
        }

        @SuppressWarnings("unchecked")
        Map<String, String> locator = (Map<String, String>) locatorObj;

        // Validate required fields
        if (!locator.containsKey("type") || !locator.containsKey("value")) {
            throw new IllegalArgumentException("Locator must contain 'type' and 'value' fields");
        }

        return locator;
    }


    private By getLocator(Map<String, String> locator) {
        String type = locator.get("type");
        String value = locator.get("value");

        return switch (type.toLowerCase()) {
            case "id" -> By.id(value);
            case "name" -> By.name(value);
            case "xpath" -> By.xpath(value);
            case "cssselector" -> By.cssSelector(value);
            default -> throw new IllegalArgumentException("Unsupported locator type: " + type);
        };

    }
}