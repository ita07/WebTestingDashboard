package com.ita07.webTestingDashboard.repository;

import com.ita07.webTestingDashboard.model.TestSuite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestSuiteRepository extends JpaRepository<TestSuite, Long> {
    // Additional query methods can be added here if needed
}

