package com.ita07.webTestingDashboard.controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import com.ita07.webTestingDashboard.serviceImpl.SharedDataServiceImpl;
import com.ita07.webTestingDashboard.utils.FormatUtils;
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

    @Autowired
    private FormatUtils formatUtils;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/analytics")
    public String getAnalytics(Model model) {
        // Add any data needed for analytics here
        long totalTestRuns = sharedDataService.getTotalTestRuns();

        long successfulTestRuns = sharedDataService.getSuccessfulTestRuns();
        double successRate = totalTestRuns > 0 ? (successfulTestRuns * 100.0 / totalTestRuns) : 0;

        double avgDuration = sharedDataService.getAverageTestRunDuration();

        model.addAttribute("successRate", formatUtils.formatPercentage(successRate));
        model.addAttribute("avgDuration", formatUtils.formatDuration(avgDuration));

        // Set layout attributes
        model.addAttribute("activeTab", "analytics");
        model.addAttribute("pageTitle", "Analytics - Web Testing");
        model.addAttribute("view", "analytics"); // New way, aligns with layout.html

        return "layout";
    }
}
