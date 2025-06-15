package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestRequest;
import com.ita07.webTestingDashboard.model.TestRun;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import com.ita07.webTestingDashboard.service.TestService;
import com.ita07.webTestingDashboard.serviceImpl.TestServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private static final Logger logger = LoggerFactory.getLogger(TestController.class);
    private final TestService testService;
    private final TestRunRepository testRunRepository;

    @Autowired
    private TestServiceImpl testServiceImpl;

    @Autowired
    public TestController(TestService testService, TestRunRepository testRunRepository) {
        this.testService = testService;
        this.testRunRepository = testRunRepository;
    }

    @PostMapping("/run")
    public Map<String, Object> runTests(@RequestBody TestRequest request) {
        int testRunId = testService.submitTestAsync(request);
        Map<String, Object> response = new HashMap<>();
        response.put("testRunId", testRunId);
        return response;
    }

    @GetMapping("/results/{testRunId}")
    public Map<String, Object> getTestResults(@PathVariable int testRunId) {
        Map<String, Object> response = new HashMap<>();
        List<ActionResult> results = TestServiceImpl.getResultsForRun(testRunId); // From in-memory map
        boolean isTaskActive = TestServiceImpl.isTestFutureActive(testRunId); // Use new static method

        String determinedStatus;
        if (isTaskActive) {
            determinedStatus = "running";
        } else { // Future is no longer active
            if (results != null) {
                // Results are present in the map. Test finished and populated them (or error populated empty list).
                determinedStatus = "finished";
            } else {
                // Future is done, but no results in the map.
                // This means they were either never populated or already served and cleared.
                determinedStatus = "not_found";
            }
        }

        response.put("testRunId", testRunId);
        // Only include results in the response if the status is 'finished'.
        if ("finished".equals(determinedStatus)) {
            response.put("results", results);
        } else {
            response.put("results", null); // Ensure results are null if not 'finished' or if they are meant to be absent for 'running'
        }
        response.put("status", determinedStatus);

        // Clear in-memory results if the task is done and results were successfully retrieved (are not null).
        if (!isTaskActive && results != null) { // This implies determinedStatus was "finished"
            TestServiceImpl.clearPersistedTestResults(testRunId);
            logger.info("Cleared in-memory results for testRunId {} as task is not active and results were served.", testRunId);
        }
        return response;
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

    @PostMapping("/cancel/{testRunId}")
    public Map<String, Object> cancelTestRun(@PathVariable Long testRunId) {
        boolean success = testService.cancelTestRun(testRunId);
        Map<String, Object> response = new HashMap<>();
        response.put("testRunId", testRunId);
        response.put("cancelled", success);
        return response;
    }
}