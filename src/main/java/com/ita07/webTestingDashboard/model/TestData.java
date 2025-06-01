package com.ita07.webTestingDashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class TestData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;

    @Column(columnDefinition = "jsonb")
    private String dataJson; // Store data as JSON string in jsonb column

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
