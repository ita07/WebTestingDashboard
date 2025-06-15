package com.ita07.webTestingDashboard.controller;


import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    @GetMapping("/analytics")
    public String getAnalytics(Model model) {
        // Add any data needed for analytics here
        model.addAttribute("successRate", 85.5); // Example data
        model.addAttribute("avgDuration", 2.45); // Example data in minutes

        // Set layout attributes
        model.addAttribute("activeTab", "analytics");
        model.addAttribute("pageTitle", "Analytics - Web Testing");
        model.addAttribute("view", "analytics"); // New way, aligns with layout.html

        return "layout";
    }
}
