package com.ita07.webTestingDashboard.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettingsDTO {
    private int concurrency;
    private String browser;
}
