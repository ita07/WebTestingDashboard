package com.ita07.webTestingDashboard.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita07.webTestingDashboard.model.TestRun;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import com.ita07.webTestingDashboard.serviceImpl.SharedDataServiceImpl;
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

    @Autowired
    private SharedDataServiceImpl sharedDataService;


    @GetMapping("/dashboard")
    public String getDashboard(Model model) {
        long totalTestRuns = sharedDataService.getTotalTestRuns();

        long successfulTestRuns = sharedDataService.getSuccessfulTestRuns();

        long cancelledTestRuns = sharedDataService.getCancelledTestRuns();
        long failedTestRuns = totalTestRuns - successfulTestRuns - cancelledTestRuns;

        Map<String, Object> status = testController.getTestExecutionStatus();

        model.addAttribute("totalTestRuns", totalTestRuns);
        model.addAttribute("failedTests", failedTestRuns);
        model.addAttribute("passedTests", successfulTestRuns);
        model.addAttribute("cancelledTests", cancelledTestRuns);
        model.addAttribute("runningTests", status.get("activeTestRuns"));
        model.addAttribute("queuedTests", status.get("queuedTestRuns"));

        model.addAttribute("activeTab", "dashboard");
        model.addAttribute("pageTitle", "Dashboard - Web Testing");
        model.addAttribute("view", "dashboard");

        return "layout";
    }
}