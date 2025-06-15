package com.ita07.webTestingDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportsController {

    @GetMapping("/reports")
    public String getReports(Model model) {
        // Add any data needed for reports here

        // Set layout attributes
        model.addAttribute("activeTab", "reports");
        model.addAttribute("pageTitle", "Reports - Web Testing");
        model.addAttribute("view", "reports"); // New way, aligns with layout.html

        return "layout";
    }
}
