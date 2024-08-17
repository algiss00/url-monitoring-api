package com.applifting.code_assessment.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
public class MonitoringResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime dateOfCheck;
    private Integer returnedHttpStatusCode;

    @Lob
    @Column(columnDefinition = "LONGTEXT")
    private String returnedPayload;

    @ManyToOne
    @JoinColumn(name = "endpoint_id", nullable = false)
    private MonitoredEndpoint monitoredEndpoint;

}
