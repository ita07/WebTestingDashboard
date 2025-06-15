package com.ita07.webTestingDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SettingsController {

    @GetMapping("/settings")
    public String getSettings(Model model) {
        // Add any data needed for settings here

        // Set layout attributes
        model.addAttribute("activeTab", "settings");
        model.addAttribute("pageTitle", "Settings - Web Testing");
        model.addAttribute("view", "settings"); // New way, aligns with layout.html

        return "layout";
    }
}
