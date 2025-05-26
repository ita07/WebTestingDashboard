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


    public static WebDriver createDriver(String browserType, Map<String, Object> browserOptions) {
        if (browserOptions == null) browserOptions = new HashMap<>();
        return switch (browserType.toLowerCase()) {
            case "chrome" -> {
                ChromeOptions chromeOptions = new ChromeOptions();
                // Headless
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("headless", false))) {
                    chromeOptions.addArguments("--headless=new");
                }
                // Window size
                if (browserOptions.containsKey("windowSize")) {
                    chromeOptions.addArguments("--window-size=" + browserOptions.get("windowSize"));
                } else {
                    chromeOptions.addArguments("--window-size=1920,1080");
                }
                // User agent
                if (browserOptions.containsKey("userAgent")) {
                    chromeOptions.addArguments("--user-agent=" + browserOptions.get("userAgent"));
                }
                // Language
                if (browserOptions.containsKey("lang")) {
                    chromeOptions.addArguments("--lang=" + browserOptions.get("lang"));
                } else {
                    chromeOptions.addArguments("--lang=en");
                }
                // Incognito
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("incognito", false))) {
                    chromeOptions.addArguments("--incognito");
                }
                // Disable extensions
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableExtensions", false))) {
                    chromeOptions.addArguments("--disable-extensions");
                }
                // Disable notifications
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableNotifications", true))) {
                    chromeOptions.addArguments("--disable-notifications");
                }
                // Disable GPU
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableGpu", true))) {
                    chromeOptions.addArguments("--disable-gpu");
                }
                // No sandbox
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("noSandbox", true))) {
                    chromeOptions.addArguments("--no-sandbox");
                }
                // Disable dev shm usage
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableDevShmUsage", true))) {
                    chromeOptions.addArguments("--disable-dev-shm-usage");
                }
                // Disable infobars
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableInfobars", true))) {
                    chromeOptions.addArguments("--disable-infobars");
                }
                // Disable automation controlled
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableAutomationControlled", true))) {
                    chromeOptions.addArguments("--disable-blink-features=AutomationControlled");
                }
                // Exclude switches
                chromeOptions.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
                // Proxy
                if (browserOptions.containsKey("proxy")) {
                    chromeOptions.addArguments("--proxy-server=" + browserOptions.get("proxy"));
                }
                // Download directory and block cookies
                Map<String, Object> prefs = new HashMap<>();
                if (browserOptions.containsKey("downloadDir")) {
                    prefs.put("download.default_directory", browserOptions.get("downloadDir"));
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("blockCookies", false))) {
                    prefs.put("profile.default_content_setting_values.cookies", 2);
                    prefs.put("profile.cookie_controls_mode", 1);
                }
                chromeOptions.setExperimentalOption("prefs", prefs);
                // Experimental options
                if (browserOptions.containsKey("experimentalOptions") && browserOptions.get("experimentalOptions") instanceof Map<?,?> experimentalOptions) {
                    for (Object key : experimentalOptions.keySet()) {
                        chromeOptions.setExperimentalOption(key.toString(), experimentalOptions.get(key));
                    }
                }
                // Custom args
                if (browserOptions.containsKey("args") && browserOptions.get("args") instanceof Iterable<?>) {
                    for (Object arg : (Iterable<?>) browserOptions.get("args")) {
                        chromeOptions.addArguments(arg.toString());
                    }
                }
                // Remote debugging port
                if (browserOptions.containsKey("remoteDebuggingPort")) {
                    chromeOptions.addArguments("--remote-debugging-port=" + browserOptions.get("remoteDebuggingPort"));
                }
                // Verbose logging
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("verboseLogging", false))) {
                    chromeOptions.setCapability("goog:loggingPrefs", Map.of("browser", "ALL"));
                }
                yield new ChromeDriver(chromeOptions);
            }
            case "firefox" -> {
                FirefoxOptions firefoxOptions = new FirefoxOptions();
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("headless", false))) {
                    firefoxOptions.addArguments("-headless");
                }
                if (browserOptions.containsKey("windowSize")) {
                    String[] size = browserOptions.get("windowSize").toString().split(",");
                    if (size.length == 2) {
                        firefoxOptions.addArguments("--width=" + size[0]);
                        firefoxOptions.addArguments("--height=" + size[1]);
                    }
                }
                if (browserOptions.containsKey("userAgent")) {
                    firefoxOptions.addPreference("general.useragent.override", browserOptions.get("userAgent"));
                }
                if (browserOptions.containsKey("lang")) {
                    firefoxOptions.addPreference("intl.accept_languages", browserOptions.get("lang"));
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("private", false))) {
                    firefoxOptions.addArguments("-private");
                }
                if (browserOptions.containsKey("proxy")) {
                    firefoxOptions.addPreference("network.proxy.type", 1);
                    firefoxOptions.addPreference("network.proxy.http", browserOptions.get("proxy"));
                }
                if (browserOptions.containsKey("downloadDir")) {
                    firefoxOptions.addPreference("browser.download.dir", browserOptions.get("downloadDir"));
                    firefoxOptions.addPreference("browser.download.folderList", 2);
                    firefoxOptions.addPreference("browser.helperApps.neverAsk.saveToDisk", "application/octet-stream");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("acceptInsecureCerts", false))) {
                    firefoxOptions.setAcceptInsecureCerts(true);
                }
                if (browserOptions.containsKey("profile")) {
                    firefoxOptions.setProfile(new org.openqa.selenium.firefox.FirefoxProfile(new java.io.File(browserOptions.get("profile").toString())));
                }
                if (browserOptions.containsKey("preferences") && browserOptions.get("preferences") instanceof Map<?,?> preferences) {
                    for (Object key : preferences.keySet()) {
                        firefoxOptions.addPreference(key.toString(), preferences.get(key));
                    }
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("verboseLogging", false))) {
                    System.setProperty("webdriver.firefox.logfile", "firefox.log");
                }
                yield new FirefoxDriver(firefoxOptions);
            }
            case "edge" -> {
                EdgeOptions edgeOptions = new EdgeOptions();
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("headless", false))) {
                    edgeOptions.addArguments("--headless=new");
                }
                if (browserOptions.containsKey("windowSize")) {
                    edgeOptions.addArguments("--window-size=" + browserOptions.get("windowSize"));
                } else {
                    edgeOptions.addArguments("--window-size=1920,1080");
                }
                if (browserOptions.containsKey("userAgent")) {
                    edgeOptions.addArguments("--user-agent=" + browserOptions.get("userAgent"));
                }
                if (browserOptions.containsKey("lang")) {
                    edgeOptions.addArguments("--lang=" + browserOptions.get("lang"));
                } else {
                    edgeOptions.addArguments("--lang=en");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("incognito", false))) {
                    edgeOptions.addArguments("--inprivate");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableExtensions", false))) {
                    edgeOptions.addArguments("--disable-extensions");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableNotifications", true))) {
                    edgeOptions.addArguments("--disable-notifications");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableGpu", true))) {
                    edgeOptions.addArguments("--disable-gpu");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("noSandbox", true))) {
                    edgeOptions.addArguments("--no-sandbox");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableDevShmUsage", true))) {
                    edgeOptions.addArguments("--disable-dev-shm-usage");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableInfobars", true))) {
                    edgeOptions.addArguments("--disable-infobars");
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("disableAutomationControlled", true))) {
                    edgeOptions.addArguments("--disable-blink-features=AutomationControlled");
                }
                if (browserOptions.containsKey("proxy")) {
                    edgeOptions.addArguments("--proxy-server=" + browserOptions.get("proxy"));
                }
                // Download directory and block cookies
                Map<String, Object> edgePrefs = new HashMap<>();
                if (browserOptions.containsKey("downloadDir")) {
                    edgePrefs.put("download.default_directory", browserOptions.get("downloadDir"));
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("blockCookies", false))) {
                    edgePrefs.put("profile.default_content_setting_values.cookies", 2);
                    edgePrefs.put("profile.cookie_controls_mode", 1);
                }
                edgeOptions.setExperimentalOption("prefs", edgePrefs);
                if (browserOptions.containsKey("experimentalOptions") && browserOptions.get("experimentalOptions") instanceof Map<?,?> experimentalOptions) {
                    for (Object key : experimentalOptions.keySet()) {
                        edgeOptions.setExperimentalOption(key.toString(), experimentalOptions.get(key));
                    }
                }
                if (browserOptions.containsKey("args") && browserOptions.get("args") instanceof Iterable<?>) {
                    for (Object arg : (Iterable<?>) browserOptions.get("args")) {
                        edgeOptions.addArguments(arg.toString());
                    }
                }
                if (browserOptions.containsKey("remoteDebuggingPort")) {
                    edgeOptions.addArguments("--remote-debugging-port=" + browserOptions.get("remoteDebuggingPort"));
                }
                if (Boolean.TRUE.equals(browserOptions.getOrDefault("verboseLogging", false))) {
                    edgeOptions.setCapability("ms:loggingPrefs", Map.of("browser", "ALL"));
                }
                yield new EdgeDriver(edgeOptions);
            }
            default -> throw new IllegalArgumentException("Unsupported browser type: " + browserType);
        };
    }
}
