package com.ita07.webTestingDashboard.model;

import jakarta.persistence.*;
import lombok.Data;
import com.vladmihalcea.hibernate.type.json.JsonType;
import org.hibernate.annotations.Type;
import java.time.LocalDateTime;

@Entity
@Data
public class TestSuite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @Type(JsonType.class)
    @Column(columnDefinition = "jsonb")
    private String actionsJson; // Store actions as JSON string in jsonb column
}