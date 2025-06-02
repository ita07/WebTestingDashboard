package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.Map;

public class AssertAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String details = "params: " + params.toString();
        try {
            String condition = (String) params.get("condition");
            if (condition == null || condition.isBlank()) {
                return new ActionResult("assert", "failure", "Missing condition parameter.", System.currentTimeMillis() - start, details);
            }

            condition = condition.toLowerCase();
            Map<String, String> locator = (Map<String, String>) params.get("locator");
            String expected = (String) params.get("expected");
            String attribute = (String) params.get("attribute");
            String successMessage = "Assertion passed: " + condition;
            int timeout = SeleniumUtils.getTimeout(params, 10);

            switch (condition) {
                case "text" -> {
                    if (locator == null || expected == null) {
                        return new ActionResult("assert", "failure", "Missing locator or expected value for text assertion.", System.currentTimeMillis() - start, details);
                    }
                    By by = SeleniumUtils.getByFromLocator(locator);
                    WebElement element = SeleniumUtils.waitForElementVisible(driver, by, timeout);
                    SeleniumUtils.assertTextEquals(element, expected);
                    successMessage += " (expected: '" + expected + "')";
                }
                case "title" -> {
                    if (expected == null) {
                        return new ActionResult("assert", "failure", "Missing expected value for title assertion.", System.currentTimeMillis() - start, details);
                    }
                    // Wait for title to match expected within timeout
                    boolean matched = false;
                    for (int i = 0; i < timeout; i++) {
                        if (driver.getTitle().equals(expected)) {
                            matched = true;
                            break;
                        }
                        Thread.sleep(1000);
                    }
                    if (!matched) {
                        throw new AssertionError("Expected title: " + expected + ", but found: " + driver.getTitle());
                    }
                    successMessage += " (expected: '" + expected + "')";
                }
                case "elementpresent" -> {
                    if (locator == null) {
                        return new ActionResult("assert", "failure", "Missing locator for elementPresent assertion.", System.currentTimeMillis() - start, details);
                    }
                    By by = SeleniumUtils.getByFromLocator(locator);
                    boolean found = false;
                    for (int i = 0; i < timeout; i++) {
                        if (!driver.findElements(by).isEmpty()) {
                            found = true;
                            break;
                        }
                        Thread.sleep(1000);
                    }
                    if (!found) {
                        throw new AssertionError("Element not present: " + by);
                    }
                }
                case "elementvisible" -> {
                    if (locator == null) {
                        return new ActionResult("assert", "failure", "Missing locator for elementVisible assertion.", System.currentTimeMillis() - start, details);
                    }
                    By by = SeleniumUtils.getByFromLocator(locator);
                    WebElement element = SeleniumUtils.waitForElementVisible(driver, by, timeout);
                    if (!element.isDisplayed()) {
                        throw new AssertionError("Element is not visible: " + by);
                    }
                }
                case "elementenabled" -> {
                    if (locator == null) {
                        return new ActionResult("assert", "failure", "Missing locator for elementEnabled assertion.", System.currentTimeMillis() - start, details);
                    }
                    By by = SeleniumUtils.getByFromLocator(locator);
                    WebElement element = SeleniumUtils.waitForElementVisible(driver, by, timeout);
                    if (!element.isEnabled()) {
                        throw new AssertionError("Element is not enabled: " + by);
                    }
                }
                case "attributevalue" -> {
                    if (locator == null || attribute == null || expected == null) {
                        return new ActionResult("assert", "failure", "Missing locator, attribute, or expected value for attributeValue assertion.", System.currentTimeMillis() - start, details);
                    }
                    By by = SeleniumUtils.getByFromLocator(locator);
                    WebElement element = SeleniumUtils.waitForElementVisible(driver, by, timeout);
                    SeleniumUtils.assertAttributeValue(element, attribute, expected);
                    successMessage += " (attribute: '" + attribute + "', expected: '" + expected + "')";
                }
                default -> {
                    return new ActionResult("assert", "failure", "Unsupported assertion condition: " + condition, System.currentTimeMillis() - start, details);
                }
            }
            return new ActionResult("assert", "success", successMessage, System.currentTimeMillis() - start, details);
        } catch (AssertionError e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("assert", "failure", "Assertion failed: " + errorMessage, System.currentTimeMillis() - start, details);
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("assert", "failure", errorMessage, System.currentTimeMillis() - start, details);
        }
    }
}
