package com.ita07.webTestingDashboard.serviceImpl;

import com.ita07.webTestingDashboard.model.TestSuite;
import com.ita07.webTestingDashboard.repository.TestSuiteRepository;
import com.ita07.webTestingDashboard.service.TestSuiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TestSuiteServiceImpl implements TestSuiteService {
    @Autowired
    private TestSuiteRepository testSuiteRepository;

    @Override
    public TestSuite createTestSuite(TestSuite testSuite) {
        testSuite.setCreatedAt(LocalDateTime.now());
        testSuite.setUpdatedAt(LocalDateTime.now());
        return testSuiteRepository.save(testSuite);
    }

    @Override
    public TestSuite updateTestSuite(Long id, TestSuite testSuite) {
        Optional<TestSuite> existing = testSuiteRepository.findById(id);
        if (existing.isEmpty()) {
            throw new RuntimeException("TestSuite not found with id: " + id);
        }
        TestSuite toUpdate = existing.get();
        toUpdate.setName(testSuite.getName());
        toUpdate.setDescription(testSuite.getDescription());
        toUpdate.setActionsJson(testSuite.getActionsJson());
        toUpdate.setUpdatedAt(LocalDateTime.now());
        return testSuiteRepository.save(toUpdate);
    }

    @Override
    public void deleteTestSuite(Long id) {
        testSuiteRepository.deleteById(id);
    }

    @Override
    public TestSuite getTestSuite(Long id) {
        return testSuiteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("TestSuite not found with id: " + id));
    }

    @Override
    public List<TestSuite> getAllTestSuites() {
        return testSuiteRepository.findAll();
    }
}


