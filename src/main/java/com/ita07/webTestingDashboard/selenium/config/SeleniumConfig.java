package com.ita07.webTestingDashboard.selenium.config;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SeleniumConfig {

    public static WebDriver createDriver(String browserType) {

        return switch (browserType.toLowerCase()) {
            case "chrome" -> {
                ChromeOptions chromeOptions = new ChromeOptions();

                // Create unique user data directory for each session to avoid conflicts
                String uniqueUserDataDir = "/tmp/chrome-user-data-" + UUID.randomUUID().toString();

                // Ensure the directory exists
                File userDataDir = new File(uniqueUserDataDir);
                if (!userDataDir.exists()) {
                    userDataDir.mkdirs();
                }

                chromeOptions.addArguments("--user-data-dir=" + uniqueUserDataDir);

                // Enable headless mode for Docker containers - this is more stable
                chromeOptions.addArguments("--headless");
                chromeOptions.addArguments("--window-size=1920,1080");
                chromeOptions.addArguments("--disable-infobars");
                chromeOptions.addArguments("--disable-notifications");
                chromeOptions.addArguments("--disable-gpu");
                chromeOptions.addArguments("--no-sandbox");
                chromeOptions.addArguments("--disable-dev-shm-usage");
                chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                chromeOptions.addArguments("--lang=en");
                chromeOptions.addArguments("--remote-debugging-port=0");

                // Additional options for Docker/containerized environments
                chromeOptions.addArguments("--disable-background-timer-throttling");
                chromeOptions.addArguments("--disable-backgrounding-occluded-windows");
                chromeOptions.addArguments("--disable-renderer-backgrounding");
                chromeOptions.addArguments("--disable-features=TranslateUI");
                chromeOptions.addArguments("--disable-extensions");
                chromeOptions.addArguments("--disable-default-apps");
                chromeOptions.addArguments("--no-first-run");
                chromeOptions.addArguments("--disable-background-networking");
                chromeOptions.addArguments("--disable-sync");
                chromeOptions.addArguments("--metrics-recording-only");
                chromeOptions.addArguments("--no-report-upload");

                chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

                Map<String, Object> prefs = new HashMap<>();
                prefs.put("profile.default_content_setting_values.cookies", 2);
                prefs.put("profile.cookie_controls_mode", 1);
                chromeOptions.setExperimentalOption("prefs", prefs);

                yield new ChromeDriver(chromeOptions);
            }
            case "firefox" -> {
                FirefoxOptions firefoxOptions = new FirefoxOptions();

                // Enable headless mode for Firefox - essential for Docker containers
                firefoxOptions.addArguments("--headless");

                // Explicitly set the Firefox binary path for Docker container
                firefoxOptions.setBinary("/usr/bin/firefox");

                // Create unique profile directory for Firefox
                String uniqueProfileDir = "/tmp/firefox-profile-" + UUID.randomUUID().toString();
                File profileDir = new File(uniqueProfileDir);
                if (!profileDir.exists()) {
                    profileDir.mkdirs();
                }

                // Set Firefox preferences for containerized environment
                firefoxOptions.addPreference("browser.download.folderList", 2);
                firefoxOptions.addPreference("browser.download.dir", "/tmp");
                firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream");
                firefoxOptions.addPreference("browser.download.manager.showWhenStarting", false);
                firefoxOptions.addPreference("pdfjs.disabled", true);

                // Additional Firefox-specific options for Docker containers
                firefoxOptions.addArguments("--no-sandbox");
                firefoxOptions.addArguments("--disable-dev-shm-usage");
                firefoxOptions.addArguments("--disable-gpu");
                firefoxOptions.addArguments("--window-size=1920,1080");
                firefoxOptions.addArguments("--disable-extensions");

                yield new FirefoxDriver(firefoxOptions);
            }
            case "edge" -> {
                EdgeOptions edgeOptions = new EdgeOptions();

                // Enable headless mode for Edge - essential for Docker containers
                edgeOptions.addArguments("--headless");

                // Create unique user data directory for Edge
                String uniqueUserDataDir = "/tmp/edge-user-data-" + UUID.randomUUID().toString();
                File userDataDir = new File(uniqueUserDataDir);
                if (!userDataDir.exists()) {
                    userDataDir.mkdirs();
                }

                edgeOptions.addArguments("--user-data-dir=" + uniqueUserDataDir);

                // Additional Edge options for Docker containers (similar to Chrome)
                edgeOptions.addArguments("--window-size=1920,1080");
                edgeOptions.addArguments("--disable-infobars");
                edgeOptions.addArguments("--disable-notifications");
                edgeOptions.addArguments("--disable-gpu");
                edgeOptions.addArguments("--no-sandbox");
                edgeOptions.addArguments("--disable-dev-shm-usage");
                edgeOptions.addArguments("--disable-blink-features=AutomationControlled");
                edgeOptions.addArguments("--lang=en");
                edgeOptions.addArguments("--remote-debugging-port=0");
                edgeOptions.addArguments("--disable-background-timer-throttling");
                edgeOptions.addArguments("--disable-backgrounding-occluded-windows");
                edgeOptions.addArguments("--disable-renderer-backgrounding");
                edgeOptions.addArguments("--disable-features=TranslateUI");
                edgeOptions.addArguments("--disable-extensions");
                edgeOptions.addArguments("--disable-default-apps");
                edgeOptions.addArguments("--no-first-run");
                edgeOptions.addArguments("--disable-background-networking");
                edgeOptions.addArguments("--disable-sync");
                edgeOptions.addArguments("--metrics-recording-only");
                edgeOptions.addArguments("--no-report-upload");

                edgeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});

                Map<String, Object> prefs = new HashMap<>();
                prefs.put("profile.default_content_setting_values.cookies", 2);
                prefs.put("profile.cookie_controls_mode", 1);
                edgeOptions.setExperimentalOption("prefs", prefs);

                yield new EdgeDriver(edgeOptions);
            }

            // Add more browsers as needed

            default -> throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        };
    }
}
