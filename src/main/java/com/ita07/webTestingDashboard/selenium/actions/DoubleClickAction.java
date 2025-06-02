package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.Map;

public class DoubleClickAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String details = "params: " + params.toString();
        try {
            Map<String, String> locator = (Map<String, String>) params.get("locator");
            if (locator == null || locator.get("type") == null || locator.get("value") == null) {
                return new ActionResult("doubleclick", "failure", "Missing locator parameter.", System.currentTimeMillis() - start, details);
            }
            By by = SeleniumUtils.getByFromLocator(locator);
            WebElement element = SeleniumUtils.findElement(driver, by);
            SeleniumUtils.doubleClickElement(driver, element);
            return new ActionResult("doubleclick", "success", "Double-clicked element.", System.currentTimeMillis() - start, "locator: " + locator);
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("doubleclick", "failure", errorMessage, System.currentTimeMillis() - start, details);
        }
    }
}
