package com.ita07.webTestingDashboard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
public class DashboardController {

    @Autowired
    private TestRunRepository testRunRepository;

    @Autowired
    private TestController testController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        long totalTestRuns = testRunRepository.count();
        model.addAttribute("totalTestRuns", totalTestRuns);

        long successfulTestRuns = testRunRepository.findAll().stream()
                .filter(testRun -> {
                    try {
                        JsonNode resultsJson = objectMapper.readTree(testRun.getResultsJson());
                        return resultsJson.findValues("status").stream()
                                .anyMatch(statusNode -> "success".equalsIgnoreCase(statusNode.asText()));
                    } catch (Exception e) {
                        return false;
                    }
                })
                .count();

        long failedTestRuns = totalTestRuns - successfulTestRuns;
        model.addAttribute("failedTests", failedTestRuns);

        double successRate = totalTestRuns > 0 ? (successfulTestRuns * 100.0 / totalTestRuns) : 0;
        model.addAttribute("successRate", successRate);
        model.addAttribute("passedTests", successfulTestRuns);

        double avgDuration = testRunRepository.findAll().stream()
                .mapToDouble(testRun -> {
                    try {
                        JsonNode resultsJson = objectMapper.readTree(testRun.getResultsJson());
                        return resultsJson.findValues("executionTimeMillis").stream()
                                .mapToDouble(JsonNode::asDouble)
                                .sum() / 60000; // Convert milliseconds to minutes
                    } catch (Exception e) {
                        return 0;
                    }
                })
                .average()
                .orElse(0);
        model.addAttribute("avgDuration", avgDuration);

        Map<String, Object> status = testController.getTestExecutionStatus();
        model.addAttribute("runningTests", status.get("activeTestRuns"));
        model.addAttribute("queuedTests", status.get("queuedTestRuns"));

        return "layout";
    }
}
