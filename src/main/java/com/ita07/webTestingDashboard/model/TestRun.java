package com.ita07.webTestingDashboard.model;

import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;

@Entity
@Data
public class TestRun {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String browser;
    private LocalDateTime executedAt;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String actionsJson; // Store actions as JSON string in jsonb column

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String resultsJson; // Store results as JSON string in jsonb column

    @Column(nullable = false)
    private String status; // Status of the test run (e.g., "running", "finished", "cancelled")
}