package com.ita07.webTestingDashboard.service;

import com.ita07.webTestingDashboard.selenium.abstractions.SeleniumAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ActionService {

    @Autowired
    private ApplicationContext applicationContext;

    public Map<String, List<String>> getSupportedActions() {
        Map<String, List<String>> actions = new HashMap<>();

        // Fetch all beans implementing SeleniumAction
        Map<String, SeleniumAction> actionBeans = applicationContext.getBeansOfType(SeleniumAction.class);

        for (Map.Entry<String, SeleniumAction> entry : actionBeans.entrySet()) {
            SeleniumAction action = entry.getValue();
            String actionName = action.getClass().getSimpleName().replace("Action", "").toLowerCase();

            List<String> parameters = action.getRequiredParameters();
            actions.put(actionName, parameters);
        }

        return actions;
    }
}
