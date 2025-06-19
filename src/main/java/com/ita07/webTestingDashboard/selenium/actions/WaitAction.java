package com.ita07.webTestingDashboard.selenium.actions;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumActionExecutor;
import com.ita07.webTestingDashboard.selenium.utils.SeleniumUtils;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class WaitAction implements SeleniumAction {
    @Override
    public ActionResult execute(WebDriver driver, Map<String, Object> params) {
        long start = System.currentTimeMillis();
        String details = "params: " + params.toString();
        try {
            Object secondsObj = params.get("seconds");
            int seconds;

            if (secondsObj == null) {
                return new ActionResult("wait", "failure", "Missing 'seconds' parameter.", System.currentTimeMillis() - start, details);
            }

            try {
                if (secondsObj instanceof Number) {
                    seconds = ((Number) secondsObj).intValue();
                } else {
                    seconds = Integer.parseInt(secondsObj.toString());
                }
            } catch (NumberFormatException e) {
                return new ActionResult("wait", "failure", "Invalid format for 'seconds' parameter. It must be an integer.", System.currentTimeMillis() - start, details);
            }

            if (seconds < 0) {
                return new ActionResult("wait", "failure", "'seconds' must be a non-negative integer.", System.currentTimeMillis() - start, details);
            }

            SeleniumUtils.waitSeconds(seconds);
            return new ActionResult("wait", "success", "Waited for " + seconds + " seconds.", System.currentTimeMillis() - start, "seconds: " + seconds);
        } catch (Exception e) {
            String errorMessage = SeleniumActionExecutor.extractErrorMessage(e);
            // Check if the error is from our own RuntimeException in SeleniumUtils.waitSeconds (due to InterruptedException)
            if (e instanceof RuntimeException && e.getMessage() != null && e.getMessage().startsWith("Thread was interrupted")) {
                errorMessage = "Wait action was interrupted."; // Provide a more specific message for interruption
            }
            return new ActionResult("wait", "failure", errorMessage, System.currentTimeMillis() - start, details);
        }
    }

    @Override
    public List<String> getRequiredParameters() {
        return List.of("seconds");
    }
}
