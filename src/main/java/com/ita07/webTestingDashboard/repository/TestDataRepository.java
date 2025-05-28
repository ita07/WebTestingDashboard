package com.ita07.webTestingDashboard.repository;

import com.ita07.webTestingDashboard.model.TestData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestDataRepository extends JpaRepository<TestData, Long> {
    // Add any specific query methods if needed
}
