package com.ita07.webTestingDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller for handling simple redirections
 */
@Controller
public class RedirectController {

    /**
     * Redirect root path to /dashboard
     */
    @GetMapping("/")
    public String redirectToDashboard() {
        return "redirect:/dashboard";
    }
}
