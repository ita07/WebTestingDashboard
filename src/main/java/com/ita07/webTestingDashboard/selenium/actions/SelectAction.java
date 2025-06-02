package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor; // Import added
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.Map;

public class SelectAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String details = "params: " + params.toString();
        try {
            Map<String, String> locator = (Map<String, String>) params.get("locator");
            String selectBy = (String) params.get("selectBy");
            String option = (String) params.get("option");
            if (locator == null || locator.get("type") == null || locator.get("value") == null || selectBy == null || option == null) {
                return new ActionResult("select", "failure", "Missing locator, selectBy, or option parameter.", System.currentTimeMillis() - start, details);
            }
            int timeout = SeleniumUtils.getTimeout(params, 10);
            By by = SeleniumUtils.getByFromLocator(locator);
            WebElement element = SeleniumUtils.waitForElementVisible(driver, by, timeout);
            SeleniumUtils.selectDropdownOption(element, selectBy, option);
            return new ActionResult("select", "success", "Selected option '" + option + "' by " + selectBy, System.currentTimeMillis() - start, "locator: " + locator + ", selectBy: " + selectBy + ", option: " + option);
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("select", "failure", errorMessage, System.currentTimeMillis() - start, details);
        }
    }
}

