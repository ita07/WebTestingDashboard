package com.ita07.webTestingDashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
public class TestRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String browser;
    private LocalDateTime executedAt;

    @Lob
    private String actionsJson; // Store actions as JSON string

    @Lob
    private String resultsJson; // Store results as JSON string
}

