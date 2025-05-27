package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestRequest;
import com.ita07.webTestingDashboard.model.TestRun;
import com.ita07.webTestingDashboard.service.TestService;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final TestService testService;
    private final TestRunRepository testRunRepository;

    @Autowired
    public TestController(TestService testService, TestRunRepository testRunRepository) {
        this.testService = testService;
        this.testRunRepository = testRunRepository;
    }

    @PostMapping("/run")
    public List<ActionResult> runTests(@RequestBody TestRequest request) {
        return testService.executeActions(request);
    }

    @GetMapping("/history")
    public List<TestRun> getTestHistory() {
        return testRunRepository.findAll();
    }
}
