package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.User;
import com.applifting.code_assessment.repositories.MonitoredEndpointRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MonitoredEndpointService {

    private final MonitoredEndpointRepository monitoredEndpointRepository;
    private final UserService userService;
    private final MonitoringService monitoringService;

    @Autowired
    public MonitoredEndpointService(MonitoredEndpointRepository monitoredEndpointRepository, UserService userService, MonitoringService monitoringService) {
        this.monitoredEndpointRepository = monitoredEndpointRepository;
        this.userService = userService;
        this.monitoringService = monitoringService;
    }

    public MonitoredEndpoint createMonitoredEndpoint(String accessToken, MonitoredEndpoint endpoint) {
        User user = userService.getUserByAccessToken(accessToken);
        endpoint.setOwner(user);
        endpoint.setDateOfCreation(LocalDateTime.now());

        MonitoredEndpoint createdEndpoint = monitoredEndpointRepository.save(endpoint);
        monitoringService.scheduleEndpointMonitoring(createdEndpoint);
        return createdEndpoint;
    }

    public List<MonitoredEndpoint> getAllEndpointsForUser(String accessToken) {
        User user = userService.getUserByAccessToken(accessToken);
        return monitoredEndpointRepository.findByOwner(user);
    }

    public Optional<MonitoredEndpoint> updateMonitoredEndpoint(String accessToken, Long id, MonitoredEndpoint endpoint) {
        User user = userService.getUserByAccessToken(accessToken);
        Optional<MonitoredEndpoint> existingEndpoint = monitoredEndpointRepository.findById(id);
        if (existingEndpoint.isPresent() && existingEndpoint.get().getOwner().getId().equals(user.getId())) {
            updateEndpointWithPersistentData(user, id, endpoint, existingEndpoint.get());
            MonitoredEndpoint updatedEndpoint = monitoredEndpointRepository.save(endpoint);

            monitoringService.stopMonitoring(id);
            monitoringService.scheduleEndpointMonitoring(updatedEndpoint);
            return Optional.of(updatedEndpoint);
        }
        return Optional.empty();
    }

    public boolean deleteMonitoredEndpoint(String accessToken, Long id) {
        User user = userService.getUserByAccessToken(accessToken);
        Optional<MonitoredEndpoint> endpoint = monitoredEndpointRepository.findById(id);
        if (endpoint.isPresent() && endpoint.get().getOwner().getId().equals(user.getId())) {
            monitoredEndpointRepository.deleteById(id);
            monitoringService.stopMonitoring(id);
            return true;
        }
        return false;
    }

    public Optional<MonitoredEndpoint> findById(Long endpointId) {
        return monitoredEndpointRepository.findById(endpointId);
    }

    public List<MonitoredEndpoint> findAll() {
        return monitoredEndpointRepository.findAll();
    }

    private void updateEndpointWithPersistentData(User user, Long id, MonitoredEndpoint endpoint, MonitoredEndpoint existingEndpoint) {
        endpoint.setId(id);
        endpoint.setOwner(user);
        endpoint.setDateOfCreation(existingEndpoint.getDateOfCreation());
        endpoint.setDateOfLastCheck(existingEndpoint.getDateOfLastCheck());
    }

}
