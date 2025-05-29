package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestRequest;
import com.ita07.webTestingDashboard.model.TestRun;
import com.ita07.webTestingDashboard.service.TestService;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import com.ita07.webTestingDashboard.serviceImpl.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.HashMap;
import java.util.Map;

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

    @GetMapping("/status")
    public Map<String, Object> getTestExecutionStatus() {
        Map<String, Object> status = new HashMap<>();
        ThreadPoolExecutor executor = TestServiceImpl.getExecutorService();
        status.put("maxParallelTests", executor.getMaximumPoolSize());
        status.put("activeTestRuns", executor.getActiveCount());
        status.put("queuedTestRuns", TestServiceImpl.getQueuedCount());
        return status;
    }
}
