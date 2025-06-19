package com.ita07.webTestingDashboard.service;

import com.ita07.webTestingDashboard.model.TestRun;

public interface SharedDataService {
    long getTotalTestRuns();
    boolean isTestRunSuccessful(TestRun testRun);
}
