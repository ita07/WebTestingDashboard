package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.config.EnvConfigService;
import com.ita07.webTestingDashboard.serviceImpl.TestServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/update")
    public ResponseEntity<String> updateSettings(
            @RequestParam("concurrency") int concurrency,
            @RequestParam("browser") String browser,
            @RequestParam("stopOnFailure") String stopOnFailure) {

        // Save to env.properties
        envConfigService.setProperty("parallel.tests.max", String.valueOf(concurrency));
        envConfigService.setProperty("browser", browser); // using "browser" as key
        envConfigService.setProperty("stop.on.failure", stopOnFailure);

        // Apply new concurrency value live
        testServiceImpl.setMaxParallelTests(concurrency);

        return ResponseEntity.ok("Settings updated successfully.");
    }
}
