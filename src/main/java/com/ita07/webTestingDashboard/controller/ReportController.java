package com.ita07.webTestingDashboard.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Comparator;

@Controller
@RequestMapping("/reports-viewer")
public class ReportController {

    @GetMapping
    public String viewReports(Model model) {
        File reportsDir = new File("reports");
        List<ReportInfo> reports = new ArrayList<>();

        if (reportsDir.exists() && reportsDir.isDirectory()) {
            File[] files = reportsDir.listFiles((dir, name) -> name.endsWith(".html"));
            if (files != null) {
                reports = Arrays.stream(files)
                    .map(file -> new ReportInfo(
                        file.getName(),
                        file.lastModified(),
                        "/reports/" + file.getName()
                    ))
                    .sorted(Comparator.comparing(ReportInfo::timestamp).reversed())
                    .toList();
            }
        }

        model.addAttribute("reports", reports);
        return "reports";
    }

    private record ReportInfo(String name, long timestamp, String url) {}
}
