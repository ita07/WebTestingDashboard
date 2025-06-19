package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.selenium.utils.LocatorTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/locators")
public class LocatorController {

    @GetMapping("/types")
    public List<String> getLocatorTypes() {
        return LocatorTypes.getSupportedLocatorTypes();
    }
}
