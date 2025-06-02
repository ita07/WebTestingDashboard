package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.Map;

public class ClickAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        try {
            Map<String, String> locator = (Map<String, String>) params.get("locator");
            if (locator == null || locator.get("type") == null || locator.get("value") == null) {
                return new ActionResult("click", "failure", "Missing locator parameters.", System.currentTimeMillis() - start, params.toString());
            }
            int timeout = SeleniumUtils.getTimeout(params, 10);
            By by = SeleniumUtils.getByFromLocator(locator);
            // Use explicit wait for clickability
            WebElement element = SeleniumUtils.waitForElementClickable(driver, by, timeout);
            SeleniumUtils.clickElement(element);
            return new ActionResult("click", "success", "Clicked element.", System.currentTimeMillis() - start, locator.toString());
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("click", "failure", errorMessage, System.currentTimeMillis() - start, params.toString());
        }
    }
}

