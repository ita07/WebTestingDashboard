package com.ita07.webTestingDashboard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita07.webTestingDashboard.model.TestReport;
import com.ita07.webTestingDashboard.repository.TestReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ReportsController {

    @Autowired
    private TestReportRepository testReportRepository;

    @GetMapping("/reports")
    public String getReports(Model model) {
        // Fetch all reports from the database
        List<TestReport> reports = testReportRepository.findAll();

        ObjectMapper objectMapper = new ObjectMapper();
        // Map totalExecutionTimeSeconds for each report dynamically
        Map<Long, Double> executionTimes = new HashMap<>();
        reports.forEach(report -> {
            if (report.getTestRun() != null) {
                try {
                    JsonNode resultsJson = objectMapper.readTree(report.getTestRun().getResultsJson());
                    double totalExecutionTimeSeconds = resultsJson.findValues("executionTimeMillis").stream()
                            .mapToDouble(JsonNode::asDouble)
                            .sum() / 1000.0; // Convert milliseconds to seconds
                    executionTimes.put(report.getId(), totalExecutionTimeSeconds);
                } catch (Exception e) {
                    executionTimes.put(report.getId(), 0.0); // Default to 0 on error
                }
            }
        });

        model.addAttribute("reports", reports);
        model.addAttribute("executionTimes", executionTimes);

        // Set layout attributes
        model.addAttribute("activeTab", "reports");
        model.addAttribute("pageTitle", "Reports - Web Testing");
        model.addAttribute("view", "reports"); // New way, aligns with layout.html

        return "layout";
    }
}