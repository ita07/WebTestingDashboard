package com.ita07.webTestingDashboard.service;

import java.util.List;
import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestRequest;

public interface TestService {
    List<ActionResult> executeActions(TestRequest request);
    boolean cancelTestRun(long testRunId);
    int submitTestAsync(TestRequest request);
}
