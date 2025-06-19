package com.ita07.webTestingDashboard.controller;

import com.ita07.webTestingDashboard.selenium.utils.SelectByTypes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/select-by")
public class SelectByController {

    @GetMapping("/types")
    public List<String> getSelectByTypes() {
        return SelectByTypes.getSupportedSelectByTypes();
    }
}
