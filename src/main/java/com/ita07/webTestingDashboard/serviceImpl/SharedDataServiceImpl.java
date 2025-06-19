package com.ita07.webTestingDashboard.serviceImpl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ita07.webTestingDashboard.controller.DashboardController;
import com.ita07.webTestingDashboard.model.TestRun;
import com.ita07.webTestingDashboard.repository.TestRunRepository;
import com.ita07.webTestingDashboard.service.SharedDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.StreamSupport;

@Service
public class SharedDataServiceImpl implements SharedDataService {

    private static final Logger logger = LoggerFactory.getLogger(SharedDataServiceImpl.class);

    @Autowired
    private TestRunRepository testRunRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public long getTotalTestRuns() {
        return testRunRepository.count();
    }

    public boolean isTestRunSuccessful(TestRun testRun) {
        try {
            JsonNode resultsJson = objectMapper.readTree(testRun.getResultsJson());
            return StreamSupport.stream(resultsJson.spliterator(), false)
                    .allMatch(actionNode -> "success".equalsIgnoreCase(actionNode.path("status").asText()));
        } catch (Exception e) {
            logger.error("Failed to parse resultsJson for testRun ID {}: {}", testRun.getId(), e.getMessage());
            return false;
        }
    }
}
