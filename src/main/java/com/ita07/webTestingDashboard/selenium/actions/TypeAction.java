package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TypeAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String details = "params: " + params.toString();
        try {
            Map<String, String> locator = (Map<String, String>) params.get("locator");
            String text = (String) params.get("text");
            if (locator == null || locator.get("type") == null || locator.get("value") == null || text == null) {
                return new ActionResult("type", "failure", "Missing locator or text parameter.", System.currentTimeMillis() - start, details);
            }
            int timeout = SeleniumUtils.getTimeout(params, 10);
            By by = SeleniumUtils.getByFromLocator(locator);
            WebElement element = SeleniumUtils.waitForElementVisible(driver, by, timeout);
            SeleniumUtils.enterText(element, text);
            return new ActionResult("type", "success", "Typed text: '" + text + "'", System.currentTimeMillis() - start, "locator: " + locator + ", text: " + text);
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("type", "failure", errorMessage, System.currentTimeMillis() - start, details);
        }
    }

    @Override
    public List<String> getRequiredParameters() {
        return List.of("locator.type", "locator.value", "text");
    }
}
