package com.ita07.webTestingDashboard.service;

import com.ita07.webTestingDashboard.model.TestSuite;
import java.util.List;

public interface TestSuiteService {
    TestSuite createTestSuite(TestSuite testSuite);
    TestSuite updateTestSuite(Long id, TestSuite testSuite);
    void deleteTestSuite(Long id);
    TestSuite getTestSuite(Long id);
    List<TestSuite> getAllTestSuites();
}

