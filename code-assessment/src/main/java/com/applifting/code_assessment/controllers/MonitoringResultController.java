package com.applifting.code_assessment.controllers;

import com.applifting.code_assessment.domain.MonitoringResult;
import com.applifting.code_assessment.services.MonitoringResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/monitoring-app/results")
public class MonitoringResultController {

    private final MonitoringResultService monitoringResultService;

    @Autowired
    public MonitoringResultController(MonitoringResultService monitoringResultService) {
        this.monitoringResultService = monitoringResultService;
    }

    @GetMapping("/{endpointId}")
    public ResponseEntity<List<MonitoringResult>> getLast10Results(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long endpointId) {
        List<MonitoringResult> results = monitoringResultService.getLast10ResultsForUserAndEndpoint(accessToken, endpointId);
        return ResponseEntity.ok(results);
    }

}
