package com.ita07.webTestingDashboard.service;

import com.ita07.webTestingDashboard.model.TestData;
import java.util.List;
import java.util.Map;

public interface TestDataService {
    TestData createTestData(TestData testData);
    TestData updateTestData(Long id, TestData testData);
    void deleteTestData(Long id);
    TestData getTestData(Long id);
    List<TestData> getAllTestData();
    Map<String, Object> resolveVariables(Map<String, Object> action, Map<String, Object> testData);
}
