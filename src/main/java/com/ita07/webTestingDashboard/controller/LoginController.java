package com.ita07.webTestingDashboard.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("applicationName", applicationName);
        model.addAttribute("environment", activeProfile.toUpperCase());
        return "login";
    }
}
