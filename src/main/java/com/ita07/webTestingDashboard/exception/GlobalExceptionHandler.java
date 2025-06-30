package com.ita07.webTestingDashboard.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NoHandlerFoundException.class)
    public ModelAndView handleNotFound(HttpServletRequest request, NoHandlerFoundException ex) {
        ModelAndView modelAndView = new ModelAndView("layout");

        modelAndView.addObject("errorTitle", "Page Not Found");
        modelAndView.addObject("errorMessage", "The page you are looking for could not be found.");
        modelAndView.addObject("statusCode", 404);
        modelAndView.addObject("pageTitle", "Page Not Found - Web Testing Dashboard");
        modelAndView.addObject("view", "error");

        return modelAndView;
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ValidationException ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("error", "Validation Error");
        response.put("message", ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(Exception.class)
    public ModelAndView handleGenericException(HttpServletRequest request, Exception ex) {
        // Check if this is an AJAX request
        String requestedWith = request.getHeader("X-Requested-With");
        if ("XMLHttpRequest".equals(requestedWith)) {
            // For AJAX requests, return JSON error response
            ModelAndView modelAndView = new ModelAndView("jsonView");
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Internal Server Error");
            response.put("message", "An unexpected error occurred. Please try again.");
            modelAndView.addObject("response", response);
            return modelAndView;
        }

        // For regular requests, return error page
        ModelAndView modelAndView = new ModelAndView("layout");

        modelAndView.addObject("errorTitle", "Internal Server Error");
        modelAndView.addObject("errorMessage", "Something went wrong on our end. Please try again later.");
        modelAndView.addObject("statusCode", 500);
        modelAndView.addObject("pageTitle", "Error - Web Testing Dashboard");
        modelAndView.addObject("view", "error");

        return modelAndView;
    }
}
