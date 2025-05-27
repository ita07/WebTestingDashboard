package com.ita07.webTestingDashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActionResult {
    private String action;
    private String status; // success or failure
    private String message; // error or info message
    private String screenshotPath; // Screenshot file path

    // Constructor for success without screenshot
    public ActionResult(String action, String status, String message) {
        this(action, status, message, null);
    }
}
