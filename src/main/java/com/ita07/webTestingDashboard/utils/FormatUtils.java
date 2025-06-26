package com.ita07.webTestingDashboard.utils;

import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class FormatUtils {

    public String formatDuration(double seconds) {
        if (seconds < 60) {
            return String.format("%.2f sec", seconds);
        } else if (seconds < 3600) {
            return String.format("%.2f min", seconds / 60);
        } else {
            return String.format("%.2f h", seconds / 3600);
        }
    }

    public String formatPercentage(double value) {
        return String.format("%.2f%%", value);
    }
}
