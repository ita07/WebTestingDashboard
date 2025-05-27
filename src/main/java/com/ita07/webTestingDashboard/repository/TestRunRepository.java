package com.ita07.webTestingDashboard.repository;

import com.ita07.webTestingDashboard.model.TestRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestRunRepository extends JpaRepository<TestRun, Long> {
    // Additional query methods can be added here if needed
}

