package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.model.TestSuite;
import com.ita07.webTestingDashboard.service.TestSuiteService;
import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestRequest;
import com.ita07.webTestingDashboard.service.TestService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/suites")
public class TestSuiteController {
    private final TestSuiteService testSuiteService;
    private final TestService testService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public TestSuiteController(TestSuiteService testSuiteService, TestService testService) {
        this.testSuiteService = testSuiteService;
        this.testService = testService;
    }

    @PostMapping
    public TestSuite createTestSuite(@RequestBody TestSuite testSuite) {
        return testSuiteService.createTestSuite(testSuite);
    }

    @PutMapping("/{id}")
    public TestSuite updateTestSuite(@PathVariable Long id, @RequestBody TestSuite testSuite) {
        return testSuiteService.updateTestSuite(id, testSuite);
    }

    @DeleteMapping("/{id}")
    public void deleteTestSuite(@PathVariable Long id) {
        testSuiteService.deleteTestSuite(id);
    }

    @GetMapping("/{id}")
    public TestSuite getTestSuite(@PathVariable Long id) {
        return testSuiteService.getTestSuite(id);
    }

    @GetMapping
    public List<TestSuite> getAllTestSuites() {
        return testSuiteService.getAllTestSuites();
    }

    @PostMapping("/{id}/run")
    public List<ActionResult> runTestSuite(@PathVariable Long id, @RequestParam(value = "browser", required = false) String browserOverride) throws Exception {
        TestSuite suite = testSuiteService.getTestSuite(id);
        Map<String, Object> actionsMap = objectMapper.readValue(suite.getActionsJson(), new TypeReference<>() {});
        String browser = browserOverride != null ? browserOverride : (String) actionsMap.getOrDefault("browser", "chrome");
        Object actionsObj = actionsMap.get("actions");
        if (actionsObj == null) {
            throw new RuntimeException("actionsJson does not contain an 'actions' array");
        }
        List<Map<String, Object>> safeActions = objectMapper.convertValue( actionsObj, new TypeReference<>() {});
        TestRequest request = new TestRequest();
        request.setBrowser(browser);
        request.setActions(safeActions);
        return testService.executeActions(request);
    }
}
