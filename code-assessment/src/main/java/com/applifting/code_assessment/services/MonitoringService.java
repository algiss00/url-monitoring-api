package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.MonitoringResult;
import com.applifting.code_assessment.repositories.MonitoredEndpointRepository;
import com.applifting.code_assessment.repositories.MonitoringResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

@Service
public class MonitoringService {

    private final MonitoredEndpointRepository monitoredEndpointRepository;
    private final MonitoringResultRepository monitoringResultRepository;
    private final TaskScheduler taskScheduler;
    private final RestTemplate restTemplate;
    private final Map<Long, ScheduledFuture<?>> scheduledTasks = new HashMap<>();

    @Autowired
    public MonitoringService(MonitoredEndpointRepository monitoredEndpointRepository, MonitoringResultRepository monitoringResultRepository, TaskScheduler taskScheduler) {
        this.monitoredEndpointRepository = monitoredEndpointRepository;
        this.monitoringResultRepository = monitoringResultRepository;
        this.taskScheduler = taskScheduler;
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void scheduleAllEndpoints() {
        List<MonitoredEndpoint> endpoints = monitoredEndpointRepository.findAll();
        for (MonitoredEndpoint endpoint : endpoints) {
            scheduleEndpointMonitoring(endpoint);
        }
    }

    public void scheduleEndpointMonitoring(MonitoredEndpoint endpoint) {
        Runnable task = () -> monitorEndpoint(endpoint);
        long intervalInMilliseconds = endpoint.getMonitoredInterval() * 1000L; // ms
        ScheduledFuture<?> future = taskScheduler.scheduleAtFixedRate(task, Duration.ofMillis(intervalInMilliseconds));
        scheduledTasks.put(endpoint.getId(), future);
    }

    public void monitorEndpoint(MonitoredEndpoint endpoint) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(endpoint.getUrl(), String.class);

            MonitoringResult result = new MonitoringResult();
            result.setMonitoredEndpoint(endpoint);
            result.setDateOfCheck(LocalDateTime.now());
            result.setReturnedHttpStatusCode(response.getStatusCode().value());
            result.setReturnedPayload(response.getBody());

            monitoringResultRepository.save(result);

            endpoint.setDateOfLastCheck(LocalDateTime.now());
            monitoredEndpointRepository.save(endpoint);
        } catch (Exception e) {
            // Zpracování chyb, například logování
            System.out.println("Cant reach url: " + endpoint.getUrl());
        }
    }

    public void stopMonitoring(Long endpointId) {
        ScheduledFuture<?> future = scheduledTasks.get(endpointId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(endpointId);
        }
    }

}