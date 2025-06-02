package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import org.openqa.selenium.WebDriver;

import java.util.Map;

public class NavigateAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String url = params.get("url") instanceof String ? (String) params.get("url") : null;
        if (url == null || url.isBlank()) {
            return new ActionResult("navigate", "failure", "Missing or empty 'url' parameter.", System.currentTimeMillis() - start, "params: " + params);
        }
        try {
            driver.get(url);
            return new ActionResult("navigate", "success", "Navigated to URL: " + url, System.currentTimeMillis() - start, "url: " + url);
        } catch (Exception e) {
            // Use SeleniumActionExecutor.extractErrorMessage to get a cleaner message
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("navigate", "failure", "Navigation failed: " + errorMessage, System.currentTimeMillis() - start, "url: " + url);
        }
    }
}

