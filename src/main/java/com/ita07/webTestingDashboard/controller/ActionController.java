package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.selenium.utils.LocatorTypes;
import com.ita07.webTestingDashboard.selenium.utils.SelectByTypes;
import com.ita07.webTestingDashboard.service.ActionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actions")
public class ActionController {

    @Autowired
    private ActionService actionService;

    @GetMapping
    public Map<String, List<String>> getSupportedActions() {
        return actionService.getSupportedActions();
    }

    @GetMapping("/locators")
    public List<String> getLocatorTypes() {
        return LocatorTypes.getSupportedLocatorTypes();
    }

    @GetMapping("/select-by")
    public List<String> getSelectByTypes() {
        return SelectByTypes.getSupportedSelectByTypes();
    }
}

