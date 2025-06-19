package com.ita07.webTestingDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestBuilderController {

    @GetMapping("/test-builder")
    public String showTestBuilder(Model model) {
        model.addAttribute("pageTitle", "Test Builder");
        model.addAttribute("activeTab", "test-builder");
        model.addAttribute("view", "test-builder");
        return "layout";
    }
}
