package com.ita07.webTestingDashboard;

import com.ita07.webTestingDashboard.selenium.config.SeleniumConfig;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class SeleniumRunner {

    public static void main(String[] args) {
        // Specify the browser type (e.g., "chrome", "firefox", "edge")
        String browserType = "chrome"; // Change this to test other browsers

        // Create a WebDriver instance with custom options
        WebDriver driver = SeleniumConfig.createDriver(browserType);

        try {
            // Step 1: Navigate to Google
            SeleniumUtils.navigateTo(driver, "https://www.google.com ");
            SeleniumUtils.handleGoogleConsentDialog(driver);
            // Step 2: Wait for the search box to be visible and enter text
            WebElement searchBox = SeleniumUtils.waitForElementVisible(driver, By.name("q"), 10);
            SeleniumUtils.enterText(searchBox, "Selenium Automation");

            // Step 3: Wait for the search button to be clickable and click it
            WebElement searchButton = SeleniumUtils.waitForElementClickable(driver, By.name("btnK"), 10);
            SeleniumUtils.clickElement(searchButton);

            // Step 4: Verify the page title contains the search term
            String pageTitle = driver.getTitle();
            assert pageTitle != null;
            if (pageTitle.contains("Selenium Automation")) {
                System.out.println("Test Passed: Page title contains 'Selenium Automation'");
            } else {
                System.out.println("Test Failed: Page title does not contain 'Selenium Automation'");
            }
        } finally {
            // Close the browser
            driver.quit();
        }
    }

}
