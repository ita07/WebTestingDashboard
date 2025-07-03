package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.model.TestReport;
import com.ita07.webTestingDashboard.repository.TestReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

@Controller
public class ReportsController {

    private static final Logger logger = LoggerFactory.getLogger(ReportsController.class);

    @Autowired
    private TestReportRepository testReportRepository;

    @GetMapping("/reports")
    public String reportsPage(@RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "10") int size,
                             Model model) {
        try {
            // Create pageable object with sorting by generatedAt desc
            Pageable pageable = PageRequest.of(page, size, Sort.by("generatedAt").descending());

            // Fetch paginated reports from the database
            Page<TestReport> reportsPage = testReportRepository.findAll(pageable);

            // Calculate execution times for each report
            Map<Long, Double> executionTimes = new HashMap<>();
            reportsPage.getContent().forEach(report -> {
                if (report.getTestRun() != null && report.getTestRun().getExecutedAt() != null) {
                    // Simple calculation - you can enhance this based on your needs
                    executionTimes.put(report.getId(), 0.0); // Default to 0 for now
                }
            });

            // Add attributes to model
            model.addAttribute("reportsPage", reportsPage);
            model.addAttribute("reports", reportsPage.getContent());
            model.addAttribute("executionTimes", executionTimes);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", reportsPage.getTotalPages());
            model.addAttribute("totalElements", reportsPage.getTotalElements());
            model.addAttribute("pageSize", size);
            model.addAttribute("hasNext", reportsPage.hasNext());
            model.addAttribute("hasPrevious", reportsPage.hasPrevious());

            // Layout attributes
            model.addAttribute("title", "Test Reports");
            model.addAttribute("activeTab", "reports");
            model.addAttribute("view", "reports");

            return "layout";
        } catch (Exception e) {
            logger.error("Error loading reports page", e);
            model.addAttribute("error", "Failed to load reports");
            return "error";
        }
    }

    @DeleteMapping("/api/reports/{id}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deleteReport(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Find the report in database
            TestReport report = testReportRepository.findById(id).orElse(null);

            if (report == null) {
                response.put("success", false);
                response.put("message", "Report not found");
                return ResponseEntity.notFound().build();
            }

            // Delete the physical file if it exists
            boolean fileDeleted = true;
            if (report.getPath() != null && !report.getPath().isEmpty()) {
                File reportFile = new File(report.getPath());
                if (reportFile.exists()) {
                    fileDeleted = reportFile.delete();
                    if (!fileDeleted) {
                        logger.warn("Failed to delete report file: {}", report.getPath());
                    }
                }
            }

            // Delete from database
            testReportRepository.deleteById(id);

            response.put("success", true);
            response.put("message", "Report deleted successfully");
            response.put("fileDeleted", fileDeleted);

            logger.info("Deleted report with ID: {}, file deleted: {}", id, fileDeleted);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error deleting report with ID: {}", id, e);
            response.put("success", false);
            response.put("message", "Failed to delete report: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    @GetMapping("/api/reports")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getReports(@RequestParam(defaultValue = "0") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by("generatedAt").descending());
            Page<TestReport> reportsPage = testReportRepository.findAll(pageable);

            Map<String, Object> response = new HashMap<>();
            response.put("content", reportsPage.getContent());
            response.put("totalPages", reportsPage.getTotalPages());
            response.put("totalElements", reportsPage.getTotalElements());
            response.put("currentPage", page);
            response.put("pageSize", size);
            response.put("hasNext", reportsPage.hasNext());
            response.put("hasPrevious", reportsPage.hasPrevious());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("Error fetching reports", e);
            Map<String, Object> response = new HashMap<>();
            response.put("error", "Failed to fetch reports: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
