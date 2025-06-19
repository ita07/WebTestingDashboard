package com.ita07.webTestingDashboard.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import com.ita07.webTestingDashboard.serviceImpl.SharedDataServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    @Autowired
    private TestRunRepository testRunRepository;

    @Autowired
    private SharedDataServiceImpl sharedDataService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/analytics")
    public String getAnalytics(Model model) {
        // Add any data needed for analytics here
        long totalTestRuns = sharedDataService.getTotalTestRuns();

        long successfulTestRuns = testRunRepository.findAll().stream()
                .filter(sharedDataService::isTestRunSuccessful)
                .count();
        double successRate = totalTestRuns > 0 ? (successfulTestRuns * 100.0 / totalTestRuns) : 0;
        model.addAttribute("successRate", successRate);

         double avgDuration = testRunRepository.findAll().stream()
                .mapToDouble(testRun -> {
                    try {
                        JsonNode resultsJson = objectMapper.readTree(testRun.getResultsJson());
                        return resultsJson.findValues("executionTimeMillis").stream()
                                .mapToDouble(JsonNode::asDouble)
                                .sum() / 1000.0; // Convert milliseconds to seconds
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .average()
                .orElse(0);
        model.addAttribute("avgDuration", avgDuration);

        // Set layout attributes
        model.addAttribute("activeTab", "analytics");
        model.addAttribute("pageTitle", "Analytics - Web Testing");
        model.addAttribute("view", "analytics"); // New way, aligns with layout.html

        return "layout";
    }
}
