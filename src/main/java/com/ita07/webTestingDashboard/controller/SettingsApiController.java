package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.config.EnvConfigService;
import com.ita07.webTestingDashboard.exception.ValidationException;
import com.ita07.webTestingDashboard.model.SettingsDTO;
import com.ita07.webTestingDashboard.service.SettingsService;
import com.ita07.webTestingDashboard.serviceImpl.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/settings")
public class SettingsApiController {

    @Autowired
    private TestServiceImpl testServiceImpl;

    @Autowired
    private EnvConfigService envConfigService;

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsDTO> getSettings() {
        SettingsDTO settings = settingsService.getCurrentSettings();
        return ResponseEntity.ok(settings);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateSettings(
            @RequestParam("concurrency") int concurrency) {

        // Validate concurrency value
        if (concurrency <= 0) {
            throw new ValidationException("Concurrency must be a positive number (minimum value is 1).");
        }

        // Save to env.properties
        envConfigService.setProperty("parallel.tests.max", String.valueOf(concurrency));
        // Apply new concurrency value live
        testServiceImpl.setMaxParallelTests(concurrency);

        return ResponseEntity.ok("Settings updated successfully.");
    }
}
