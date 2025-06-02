package com.ita07.webTestingDashboard.selenium.abstractions;

import com.ita07.webTestingDashboard.model.ActionResult;
import org.openqa.selenium.WebDriver;
import java.util.Map;

/**
 * Abstraction for a Selenium action. Each action implementation should handle its own parameter validation and execution logic.
 */
public interface SeleniumAction {
    /**
     * Executes the action using the provided WebDriver and parameters.
     * @param driver The Selenium WebDriver instance.
     * @param params The parameters for the action (from the test step JSON).
     * @return ActionResult containing the outcome of the action.
     */
    ActionResult execute(WebDriver driver, Map<String, Object> params);
}

