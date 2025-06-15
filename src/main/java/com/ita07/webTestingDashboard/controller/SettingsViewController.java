package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.config.EnvConfigService;
import com.ita07.webTestingDashboard.model.SettingsDTO;
import com.ita07.webTestingDashboard.service.SettingsService;
import com.ita07.webTestingDashboard.serviceImpl.TestServiceImpl;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsViewController {

    @Autowired
    private EnvConfigService envConfigService;

    @Autowired
    private TestServiceImpl testServiceImpl;

    @Autowired
    private SettingsService settingsService;

    @PostConstruct
    public void init() {
        String value = envConfigService.getProperty("parallel.tests.max", "4");
        int max = Integer.parseInt(value);
        testServiceImpl.setMaxParallelTests(max); // Your existing setter that initializes the executor
    }

    @GetMapping("/settings")
    public String getSettings(Model model) {
        // Add any data needed for settings here

        // Set layout attributes
        model.addAttribute("activeTab", "settings");
        model.addAttribute("pageTitle", "Settings - Web Testing");
        model.addAttribute("view", "settings"); // New way, aligns with layout.html

        SettingsDTO settings = settingsService.getCurrentSettings();
        model.addAttribute("concurrency", settings.getConcurrency());
        model.addAttribute("browser", settings.getBrowser());
        model.addAttribute("stopOnFailure", envConfigService.getProperty("stop.on.failure", "no"));

        return "layout";
    }
}
