package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.Map;

public class DragAndDropAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String details = "params: " + params.toString();
        try {
            Map<String, String> sourceLocator = (Map<String, String>) params.get("sourceLocator");
            Map<String, String> targetLocator = (Map<String, String>) params.get("targetLocator");

            if (sourceLocator == null || sourceLocator.get("type") == null || sourceLocator.get("value") == null ||
                targetLocator == null || targetLocator.get("type") == null || targetLocator.get("value") == null) {
                return new ActionResult("draganddrop", "failure", "Missing sourceLocator or targetLocator parameter.",
                    System.currentTimeMillis() - start, details);
            }

            By sourceBy = SeleniumUtils.getByFromLocator(sourceLocator);
            By targetBy = SeleniumUtils.getByFromLocator(targetLocator);

            WebElement sourceElement = SeleniumUtils.findElement(driver, sourceBy);
            WebElement targetElement = SeleniumUtils.findElement(driver, targetBy);

            SeleniumUtils.dragAndDropElement(driver, sourceElement, targetElement);
            return new ActionResult("draganddrop", "success", "Dragged and dropped element successfully.",
                System.currentTimeMillis() - start, "source: " + sourceLocator + ", target: " + targetLocator);
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("draganddrop", "failure", errorMessage,
                System.currentTimeMillis() - start, details);
        }
    }
}
