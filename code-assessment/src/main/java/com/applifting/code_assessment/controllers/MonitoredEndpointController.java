package com.applifting.code_assessment.controllers;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.services.MonitoredEndpointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/monitoring-app/endpoints")
public class MonitoredEndpointController {

    private final MonitoredEndpointService monitoredEndpointService;

    @Autowired
    public MonitoredEndpointController(MonitoredEndpointService monitoredEndpointService) {
        this.monitoredEndpointService = monitoredEndpointService;
    }

    @PostMapping
    public ResponseEntity<MonitoredEndpoint> createEndpoint(
            @RequestHeader("Authorization") String accessToken,
            @RequestBody MonitoredEndpoint endpoint) {
        MonitoredEndpoint createdEndpoint = monitoredEndpointService.createMonitoredEndpoint(accessToken, endpoint);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEndpoint);
    }

    @GetMapping
    public ResponseEntity<List<MonitoredEndpoint>> getAllEndpoints(
            @RequestHeader("Authorization") String accessToken) {
        List<MonitoredEndpoint> endpoints = monitoredEndpointService.getAllEndpointsForUser(accessToken);
        return ResponseEntity.ok(endpoints);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MonitoredEndpoint> updateEndpoint(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long id,
            @RequestBody MonitoredEndpoint endpoint) {
        Optional<MonitoredEndpoint> updatedEndpoint = monitoredEndpointService.updateMonitoredEndpoint(accessToken, id, endpoint);
        return updatedEndpoint.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.status(HttpStatus.FORBIDDEN).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEndpoint(
            @RequestHeader("Authorization") String accessToken,
            @PathVariable Long id) {
        boolean success = monitoredEndpointService.deleteMonitoredEndpoint(accessToken, id);
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
