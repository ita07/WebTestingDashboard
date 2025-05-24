package com.ita07.webTestingDashboard.selenium.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

public class SeleniumConfig {

    public static WebDriver createDriver(String browserType) {

        return switch (browserType.toLowerCase()) {
            case "chrome" -> {
                ChromeOptions chromeOptions = new ChromeOptions();
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
