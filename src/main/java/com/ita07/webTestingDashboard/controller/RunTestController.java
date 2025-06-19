package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.model.TestData;
import com.ita07.webTestingDashboard.service.TestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class RunTestController {

    @Autowired
    private TestDataService testDataService;

    @GetMapping("/run-test")
    public String getRunTestPage(Model model) {
        List<TestData> testDataList = testDataService.getAllTestData();
        model.addAttribute("testDataList", testDataList);
        model.addAttribute("activeTab", "run-test");
        model.addAttribute("pageTitle", "Run Test - Web Testing");
        model.addAttribute("view", "run-test");
        return "layout";
    }
}

