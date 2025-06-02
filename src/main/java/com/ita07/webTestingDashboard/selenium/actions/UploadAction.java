package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import java.util.Map;

public class UploadAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String details = "params: " + params.toString();
        try {
            Map<String, String> locator = (Map<String, String>) params.get("locator");
            String filePath = (String) params.get("filePath");

            if (locator == null || locator.get("type") == null || locator.get("value") == null || filePath == null || filePath.isBlank()) {
                return new ActionResult("upload", "failure", "Missing locator or filePath parameter.", System.currentTimeMillis() - start, details);
            }

            By by = SeleniumUtils.getByFromLocator(locator);
            WebElement element = SeleniumUtils.findElement(driver, by);
            SeleniumUtils.uploadFile(element, filePath);
            return new ActionResult("upload", "success", "File uploaded successfully.", System.currentTimeMillis() - start, details);
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            return new ActionResult("upload", "failure", errorMessage, System.currentTimeMillis() - start, details);
        }
    }
}
