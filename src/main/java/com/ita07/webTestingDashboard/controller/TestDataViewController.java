package com.ita07.webTestingDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestDataViewController {

    @GetMapping("/test-data")
    public String getTestDataPage(Model model) {
        // Set layout attributes
        model.addAttribute("activeTab", "test-data");
        model.addAttribute("pageTitle", "Test Data - Web Testing");
        model.addAttribute("view", "test-data");

        return "layout";
    }
}
