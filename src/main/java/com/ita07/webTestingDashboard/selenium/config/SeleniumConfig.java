package com.ita07.webTestingDashboard.selenium.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.util.HashMap;
import java.util.Map;

public class SeleniumConfig {

    public static WebDriver createDriver(String browserType) {

        return switch (browserType.toLowerCase()) {
            case "chrome" -> {
                ChromeOptions chromeOptions = new ChromeOptions();

                //chromeOptions.addArguments("--headless"); // Run in headless mode
                chromeOptions.addArguments("--window-size=1920,1080"); // Set window size
                chromeOptions.addArguments("--disable-infobars");
                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--disable-gpu"); // Disable GPU acceleration
                chromeOptions.addArguments("--no-sandbox"); // Required for sandboxed environments
                chromeOptions.addArguments("--disable-dev-shm-usage"); // Avoid issues with shared memory
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled"); // Bypass automation detection
                chromeOptions.addArguments("--lang=en"); // Force language
                chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"}); // Remove "Chrome is being controlled by automated software" message

                Map<String, Object> prefs = new HashMap<>();
                prefs.put("profile.default_content_setting_values.cookies", 2); // Block cookies (2 = block, 1 = allow)
                prefs.put("profile.cookie_controls_mode", 1); // Enable cookie controls
                chromeOptions.setExperimentalOption("prefs", prefs);

                yield new ChromeDriver(chromeOptions);
            }
            case "firefox" -> {
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                yield new FirefoxDriver(firefoxOptions);
            }
            case "edge" -> {
                EdgeOptions edgeOptions = new EdgeOptions();
                yield new EdgeDriver(edgeOptions);
            }

            // Add more browsers as needed

            default -> throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        };
    }
}
