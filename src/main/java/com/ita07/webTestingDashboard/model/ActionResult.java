package com.ita07.webTestingDashboard.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ActionResult {
    private String action;
    private String status; // success or failure
    private String message; // error or info message
    private String screenshotPath; // Screenshot file path
    private long executionTimeMillis; // Execution time for the action in milliseconds
    private String details; // Extra details about the action (parameters, etc.)

    // Add details to all constructors
    public ActionResult(String action, String status, String message, String screenshotPath, long executionTimeMillis, String details) {
        this.action = action;
        this.status = status;
        this.message = message;
        this.screenshotPath = screenshotPath;
        this.executionTimeMillis = executionTimeMillis;
        this.details = details;
    }

    // Constructor for success/failure without screenshot but with execution time
    public ActionResult(String action, String status, String message, long executionTimeMillis, String details) {
        this(action, status, message, null, executionTimeMillis, details);
    }

    // Existing constructor for success without screenshot - adapt to include execution time
    public ActionResult(String action, String status, String message, String details) {
        this(action, status, message, null, 0, details); // Defaulting execution time to 0, should be set properly
    }
}
