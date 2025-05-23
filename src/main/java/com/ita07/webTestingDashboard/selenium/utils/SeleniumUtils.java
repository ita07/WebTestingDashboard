package com.ita07.webTestingDashboard.selenium.utils;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
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

    //Handle consent dialog that pops up when browser opens initially
    public static void handleGoogleConsentDialog(WebDriver driver) {
        try {
            WebElement acceptButton = SeleniumUtils.waitForElementClickable(driver, By.id("L2AGLb"), 10);
            SeleniumUtils.clickElement(acceptButton);
            logger.info("Consent popup accepted.");
        } catch (Exception e) {
            logger.info("Consent popup not found or already accepted.");
        }
    }
}
