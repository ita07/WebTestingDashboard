package com.ita07.webTestingDashboard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita07.webTestingDashboard.model.TestRun;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;
import java.util.stream.StreamSupport;

@Controller
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);

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
                .filter(this::isTestRunSuccessful)
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

    private boolean isTestRunSuccessful(TestRun testRun) {
        try {
            JsonNode resultsJson = objectMapper.readTree(testRun.getResultsJson());
            return StreamSupport.stream(resultsJson.spliterator(), false)
                    .allMatch(actionNode -> "success".equalsIgnoreCase(actionNode.path("status").asText()));
        } catch (Exception e) {
            logger.error("Failed to parse resultsJson for testRun ID {}: {}", testRun.getId(), e.getMessage());
            return false;
        }
    }
}