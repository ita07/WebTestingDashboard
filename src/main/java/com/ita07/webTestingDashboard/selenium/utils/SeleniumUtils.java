package com.ita07.webTestingDashboard.selenium.utils;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SeleniumUtils {

    private static final Logger logger = LoggerFactory.getLogger(SeleniumUtils.class);

    // Navigate to a URL
    public static void navigateTo(WebDriver driver, String url) {
        driver.get(url);
    }

    // Find an element by locator
    public static WebElement findElement(WebDriver driver, By locator) {
        return driver.findElement(locator);
    }

    // Click an element
    public static void clickElement(WebElement element) {
        element.click();
    }

    // Enter text into an input field
    public static void enterText(WebElement element, String text) {
        element.clear();
        element.sendKeys(text);
    }

    // Hover over an element
    public static void hoverOverElement(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.moveToElement(element).perform();
    }

    // Scroll to an element using JavaScript
    public static void scrollToElement(WebDriver driver, WebElement element) {
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
    }

    // Scroll to a specific position (x, y) on the page using JavaScript
    public static void scrollToPosition(WebDriver driver, int x, int y) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(arguments[0], arguments[1]);", x, y);
    }

    // Upload a file by sending the file path to a file input element
    public static void uploadFile(WebElement element, String filePath) {
        element.sendKeys(filePath);
    }

    // Assert that the text of an element matches the expected value
    public static void assertTextEquals(WebElement element, String expectedText) {
        String actualText = element.getText();
        if (!actualText.equals(expectedText)) {
            throw new AssertionError("Expected text: " + expectedText + ", but found: " + actualText);
        }
    }

    // Assert that the page title matches the expected value
    public static void assertPageTitleEquals(WebDriver driver, String expectedTitle) {
        String actualTitle = driver.getTitle();
        if (!actualTitle.equals(expectedTitle)) {
            throw new AssertionError("Expected title: " + expectedTitle + ", but found: " + actualTitle);
        }
    }

    // Assert that an element is present in the DOM
    public static void assertElementPresent(WebDriver driver, By by) {
        if (driver.findElements(by).isEmpty()) {
            throw new AssertionError("Element not present: " + by);
        }
    }

    // Assert that an element is visible
    public static void assertElementVisible(WebDriver driver, By by) {
        WebElement element = driver.findElement(by);
        if (!element.isDisplayed()) {
            throw new AssertionError("Element is not visible: " + by);
        }
    }

    // Assert that an element is enabled
    public static void assertElementEnabled(WebDriver driver, By by) {
        WebElement element = driver.findElement(by);
        if (!element.isEnabled()) {
            throw new AssertionError("Element is not enabled: " + by);
        }
    }

    // Assert that an element's attribute matches the expected value
    public static void assertAttributeValue(WebElement element, String attribute, String expected) {
        String actual = element.getAttribute(attribute);
        if (actual == null || !actual.equals(expected)) {
            throw new AssertionError("Expected attribute '" + attribute + "' to be '" + expected + "', but was '" + actual + "'.");
        }
    }

    // Wait for an element to be visible
    public static WebElement waitForElementVisible(WebDriver driver, By locator, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    // Wait for an element to be clickable
    public static WebElement waitForElementClickable(WebDriver driver, By locator, int timeoutInSeconds) {
        WebDriverWait wait = new WebDriverWait(driver, java.time.Duration.ofSeconds(timeoutInSeconds));
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    // Static wait function (waits for X seconds)
    public static void waitSeconds(int seconds) {
        try {
            Thread.sleep(seconds * 1000L); // Convert seconds to milliseconds
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore the interrupted status
            logger.error("Thread was interrupted during wait.", e);
            throw new RuntimeException("Thread was interrupted during wait.", e);
        }
    }

    //Handle consent dialog that pops up when browser opens initially
    public static void handleGoogleConsentDialog(WebDriver driver) {
        try {
            WebElement acceptButton = SeleniumUtils.waitForElementClickable(driver, By.id("L2AGLb"), 10);
            SeleniumUtils.clickElement(acceptButton);
            logger.info("Consent popup accepted.");
        } catch (Exception e) {
            logger.info("Consent popup not found or already accepted.", e);
        }
    }

    // Select an option from a dropdown
    public static void selectDropdownOption(WebElement selectElement, String selectBy, String option) {
        Select dropdown = new Select(selectElement);
        switch (selectBy) {
            case "value" -> dropdown.selectByValue(option);
            case "visibletext" -> dropdown.selectByVisibleText(option);
            case "index" -> {
                try {
                    int idx = Integer.parseInt(option);
                    dropdown.selectByIndex(idx);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("'option' must be a valid integer for selectBy 'index'.");
                }
            }
            default -> throw new IllegalArgumentException("Unsupported selectBy: " + selectBy);
        }
    }

    // Check a checkbox or radio button if not already selected
    public static void checkElement(WebElement element) {
        if (!element.isSelected()) {
            element.click();
        }
    }

    // Uncheck a checkbox if it is selected (does nothing for radio buttons)
    public static void uncheckElement(WebElement element) {
        String type = element.getAttribute("type");
        if ("checkbox".equalsIgnoreCase(type) && element.isSelected()) {
            element.click();
        }
    }

    // Double-click an element
    public static void doubleClickElement(WebDriver driver, WebElement element) {
        Actions actions = new Actions(driver);
        actions.doubleClick(element).perform();
    }

    // Drag and drop from source to target element
    public static void dragAndDropElement(WebDriver driver, WebElement source, WebElement target) {
        Actions actions = new Actions(driver);
        actions.dragAndDrop(source, target).perform();
    }

    // Capture screenshot as base64 string
    public static String takeScreenshotAsBase64(WebDriver driver) {
        if (driver instanceof org.openqa.selenium.TakesScreenshot) {
            return ((TakesScreenshot) driver).getScreenshotAs(org.openqa.selenium.OutputType.BASE64);
        }
        return null; // Or throw an exception if screenshot capability is essential
    }
}
