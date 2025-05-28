package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.model.TestData;
import com.ita07.webTestingDashboard.service.TestDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/testdata")
public class TestDataController {

    @Autowired
    private TestDataService testDataService;

    @PostMapping
    public TestData createTestData(@RequestBody TestData testData) {
        return testDataService.createTestData(testData);
    }

    @PutMapping("/{id}")
    public TestData updateTestData(@PathVariable Long id, @RequestBody TestData testData) {
        return testDataService.updateTestData(id, testData);
    }

    @DeleteMapping("/{id}")
    public void deleteTestData(@PathVariable Long id) {
        testDataService.deleteTestData(id);
    }

    @GetMapping("/{id}")
    public TestData getTestData(@PathVariable Long id) {
        return testDataService.getTestData(id);
    }

    @GetMapping
    public List<TestData> getAllTestData() {
        return testDataService.getAllTestData();
    }
}
