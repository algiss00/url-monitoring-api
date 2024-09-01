package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.MonitoringResult;
import com.applifting.code_assessment.repositories.MonitoredEndpointRepository;
import com.applifting.code_assessment.repositories.MonitoringResultRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
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

    private static final Logger logger = LoggerFactory.getLogger(MonitoringService.class);

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

    @Transactional
    public void monitorEndpoint(MonitoredEndpoint endpoint) {
        try {
            String url = addDefaultProtocolIfMissing(endpoint.getUrl());
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

            MonitoringResult result = new MonitoringResult();
            result.setMonitoredEndpoint(endpoint);
            result.setDateOfCheck(LocalDateTime.now());
            result.setReturnedHttpStatusCode(response.getStatusCode().value());
            result.setReturnedPayload(response.getBody());

            monitoringResultRepository.save(result);

            endpoint.setDateOfLastCheck(LocalDateTime.now());
            endpoint.getMonitoringResults().add(result);
            monitoredEndpointRepository.save(endpoint);
            logger.info("Monitored endpoint {} - Status: {}", url, response.getStatusCode().value());
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Handling HTTP errors (4xx, 5xx)
            logger.error("HTTP error while monitoring endpoint {}: Status - {}, Message - {}",
                    endpoint.getUrl(), e.getStatusCode().value(), e.getMessage());
        } catch (ResourceAccessException e) {
            // Handling connection errors (e.g. timeouts)
            logger.error("Resource access error while monitoring endpoint {}: Message - {}",
                    endpoint.getUrl(), e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error while monitoring endpoint {}: Message - {}",
                    endpoint.getUrl(), e.getMessage());
        }
    }

    public void stopMonitoring(Long endpointId) {
        ScheduledFuture<?> future = scheduledTasks.get(endpointId);
        if (future != null) {
            future.cancel(false);
            scheduledTasks.remove(endpointId);
        }
    }

    private String addDefaultProtocolIfMissing(String url) {
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            return "https://" + url;
        }
        return url;
    }

}