package com.ita07.webTestingDashboard.selenium.utils;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.actions.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SeleniumActionExecutor {
    private static final Logger logger = LoggerFactory.getLogger(SeleniumActionExecutor.class);
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final WebDriver driver;
    private static final int DEFAULT_TIMEOUT = 10;

    // Action registry
    private final Map<String, SeleniumAction> actionRegistry = new HashMap<>();

    public SeleniumActionExecutor(WebDriver driver) {
        this.driver = driver;
        // Register all actions
        actionRegistry.put("navigate", new NavigateAction());
        actionRegistry.put("click", new ClickAction());
        actionRegistry.put("type", new TypeAction());
        actionRegistry.put("wait", new WaitAction());
        actionRegistry.put("hover", new HoverAction());
        actionRegistry.put("scroll", new ScrollAction());
        actionRegistry.put("upload", new UploadAction());
        actionRegistry.put("assert", new AssertAction());
        actionRegistry.put("select", new SelectAction());
        actionRegistry.put("check", new CheckAction());
        actionRegistry.put("uncheck", new UncheckAction());
        actionRegistry.put("doubleclick", new DoubleClickAction());
        actionRegistry.put("draganddrop", new DragAndDropAction());
    }

    public List<ActionResult> executeActions(List<Map<String, Object>> actions, boolean stopOnFailure) {
        List<ActionResult> results = new ArrayList<>();
        boolean failed = false;
        for (Map<String, Object> action : actions) {
            String actionType = getActionType(action);
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMATTER);
            long startTime = System.currentTimeMillis();
            long endTime;
            long durationMillis;
            String details = buildDetailsString(actionType, action);

            if (failed) {
                logger.info("[{}] Skipping action '{}' due to previous failure.", timestamp, actionType);
                durationMillis = 0; // Skipped actions have 0 duration
                results.add(new ActionResult(actionType, "skipped", "Action was not executed due to previous failure.", null, durationMillis, details));
                continue;
            }
            try {
                logger.info("[{}] Starting action: {} with parameters: {}", timestamp, actionType, action);
                SeleniumAction handler = actionRegistry.get(actionType.toLowerCase());
                if (handler == null) {
                    throw new IllegalArgumentException("Unsupported action: " + actionType);
                }
                ActionResult result = handler.execute(driver, action);
                endTime = System.currentTimeMillis();
                durationMillis = endTime - startTime;
                // If the handler did not set executionTimeMillis, set it here
                if (result.getExecutionTimeMillis() == 0) {
                    result.setExecutionTimeMillis(durationMillis);
                }
                logger.info("[{}] Action '{}' executed with status '{}' in {} ms.", timestamp, actionType, result.getStatus(), durationMillis);
                if ("failure".equalsIgnoreCase(result.getStatus())) {
                    String screenshotPath = captureScreenshot(actionType);
                    result.setScreenshotPath(screenshotPath);
                }
                results.add(result);
                if ("failure".equalsIgnoreCase(result.getStatus()) && stopOnFailure) {
                    failed = true;
                }
            } catch (Exception e) {
                endTime = System.currentTimeMillis();
                durationMillis = endTime - startTime;
                String failMessage = extractErrorMessage(e);
                logger.error("[{}] Action '{}' failed after {} ms with error: {}", timestamp, actionType, durationMillis, failMessage, e);
                String screenshotPath = captureScreenshot(actionType);
                results.add(new ActionResult(actionType, "failure", failMessage, screenshotPath, durationMillis, details));
                if (stopOnFailure) {
                    failed = true;
                }
            }
        }
        return results;
    }

    private String captureScreenshot(String actionType) {
        try {
            String screenshotsDir = "screenshots";
            Files.createDirectories(Paths.get(screenshotsDir));
            String filename = actionType + "_" + UUID.randomUUID() + ".png";
            String filePath = screenshotsDir + "/" + filename;
            String base64 = SeleniumUtils.takeScreenshotAsBase64(driver);
            if (base64 != null) {
                try (FileOutputStream fos = new FileOutputStream(filePath)) {
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64);
                    fos.write(imageBytes);
                }
                logger.info("Screenshot saved at: {}", filePath);
                // Return the path with leading forward slash for proper URL mapping
                return "/" + filePath;
            }
        } catch (Exception e) {
            logger.error("Failed to capture screenshot for action '{}': {}", actionType, e.getMessage(), e);
        }
        return null;
    }

    // Keep the original method for backward compatibility
    public List<ActionResult> executeActions(List<Map<String, Object>> actions) {
        return executeActions(actions, false);
    }

    private String getActionType(Map<String, Object> action) {
        return ((String) action.get("action")).toLowerCase();
    }

    public static String extractErrorMessage(Throwable e) { // Changed Exception to Throwable
        if (e == null) return "An unknown error occurred.";

        Throwable rootCause = e;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }

        String message = rootCause.getMessage();
        if (message == null || message.isBlank()) {
            return rootCause.getClass().getSimpleName(); // Fallback to class name if message is empty
        }

        // Take the first line of the message
        int newlineIndex = message.indexOf('\n');
        if (newlineIndex > 0) {
            return message.substring(0, newlineIndex).trim();
        }
        return message.trim(); // Return the trimmed message if no newline
    }

    private String buildDetailsString(String actionType, Map<String, Object> action) {
        StringBuilder sb = new StringBuilder();
        switch (actionType) {
            case "navigate" -> sb.append("url=").append(action.getOrDefault("url", ""));
            case "click", "check", "uncheck", "hover", "scroll", "doubleclick" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
            }
            case "type" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
                sb.append(", text=").append(action.getOrDefault("text", ""));
            }
            case "wait" -> sb.append("seconds=").append(action.getOrDefault("seconds", ""));
            case "select" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
                sb.append(", selectBy=").append(action.getOrDefault("selectBy", ""));
                sb.append(", option=").append(action.getOrDefault("option", ""));
            }
            case "upload" -> {
                sb.append("locator=").append(stringifyLocator(action.get("locator")));
                sb.append(", filePath=").append(action.getOrDefault("filePath", ""));
            }
            case "assert" -> {
                sb.append("condition=").append(action.getOrDefault("condition", ""));
                if (action.get("locator") != null) sb.append(", locator=").append(stringifyLocator(action.get("locator")));
                if (action.get("expected") != null) sb.append(", expected=").append(action.get("expected"));
                if (action.get("attribute") != null) sb.append(", attribute=").append(action.get("attribute"));
            }
            case "draganddrop" -> {
                sb.append("sourceLocator=").append(stringifyLocator(action.get("sourceLocator")));
                sb.append(", targetLocator=").append(stringifyLocator(action.get("targetLocator")));
            }
        }
        return sb.toString();
    }

    private String stringifyLocator(Object locatorObj) {
        if (locatorObj instanceof Map<?, ?> loc) {
            Object typeObj = loc.get("type");
            Object valueObj = loc.get("value");

            String type = (typeObj != null) ? String.valueOf(typeObj) : "";
            String value = (valueObj != null) ? String.valueOf(valueObj) : "";

            return "{" + type + ": '" + value + "'}";
        }
        return String.valueOf(locatorObj);
    }
}
