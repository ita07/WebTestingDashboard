package com.ita07.webTestingDashboard.controller;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        String errorMessage = "An unexpected error occurred";
        String errorTitle = "Error";

        if (status != null) {
            int statusCode = Integer.parseInt(status.toString());

            if (statusCode == HttpStatus.NOT_FOUND.value()) {
                errorTitle = "Page Not Found";
                errorMessage = "The page you are looking for could not be found.";
            } else if (statusCode == HttpStatus.INTERNAL_SERVER_ERROR.value()) {
                errorTitle = "Internal Server Error";
                errorMessage = "Something went wrong on our end. Please try again later.";
            } else if (statusCode == HttpStatus.FORBIDDEN.value()) {
                errorTitle = "Access Denied";
                errorMessage = "You don't have permission to access this resource.";
            }

            model.addAttribute("statusCode", statusCode);
        }

        model.addAttribute("errorTitle", errorTitle);
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("pageTitle", errorTitle + " - Web Testing Dashboard");
        model.addAttribute("view", "error");

        return "layout";
    }
}
