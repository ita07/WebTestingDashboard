package com.ita07.webTestingDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestRunnerController {

    @GetMapping("/test-runner")
    public String showTestRunner(Model model) {
        model.addAttribute("pageTitle", "Test Runner");
        model.addAttribute("activeTab", "test-runner");
        model.addAttribute("view", "test-runner");
        return "layout";
    }
}

