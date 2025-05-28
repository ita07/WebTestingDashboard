package com.ita07.webTestingDashboard.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

@Data
public class TestRequest {
    private String browser;
    private List<Map<String, Object>> actions;
    private boolean stopOnFailure = false;
}
