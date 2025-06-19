package com.ita07.webTestingDashboard.repository;

import com.ita07.webTestingDashboard.model.TestReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestReportRepository extends JpaRepository<TestReport, Long> {
}