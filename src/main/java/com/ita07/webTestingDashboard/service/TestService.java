package com.ita07.webTestingDashboard.service;

import java.util.List;
import java.util.Map;
import com.ita07.webTestingDashboard.model.ActionResult;
import com.ita07.webTestingDashboard.model.TestRequest;

public interface TestService {
    List<ActionResult> executeActions(TestRequest request);
}
