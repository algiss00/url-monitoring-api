package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.User;
import com.applifting.code_assessment.exceptions.ForbiddenException;
import com.applifting.code_assessment.repositories.MonitoredEndpointRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class MonitoredEndpointService {

    private final MonitoredEndpointRepository monitoredEndpointRepository;
    private final UserService userService;
    private final MonitoringService monitoringService;
    private final MessageSource messageSource;

    @Autowired
    public MonitoredEndpointService(MonitoredEndpointRepository monitoredEndpointRepository, UserService userService,
                                    MonitoringService monitoringService, MessageSource messageSource) {
        this.monitoredEndpointRepository = monitoredEndpointRepository;
        this.userService = userService;
        this.monitoringService = monitoringService;
        this.messageSource = messageSource;
    }

    @Transactional
    public MonitoredEndpoint createMonitoredEndpoint(String accessToken, MonitoredEndpoint endpoint) {
        validateEndpoint(endpoint);

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

    @Transactional
    public MonitoredEndpoint updateMonitoredEndpoint(String accessToken, Long id, MonitoredEndpoint endpoint) {
        validateEndpoint(endpoint);

        MonitoredEndpoint existingEndpoint = authorizeAndFetchEndpoint(accessToken, id, messageSource.getMessage("forbidden.update", null, LocaleContextHolder.getLocale()));
        updateEndpointWithPersistentData(existingEndpoint.getOwner(), id, endpoint, existingEndpoint);
        MonitoredEndpoint updatedEndpoint = monitoredEndpointRepository.save(endpoint);

        monitoringService.stopMonitoring(id);
        monitoringService.scheduleEndpointMonitoring(updatedEndpoint);
        return updatedEndpoint;
    }

    @Transactional
    public void deleteMonitoredEndpoint(String accessToken, Long id) {
        authorizeAndFetchEndpoint(accessToken, id, messageSource.getMessage("forbidden.delete", null, LocaleContextHolder.getLocale()));

        monitoringService.stopMonitoring(id);
        monitoredEndpointRepository.deleteById(id);
    }

    public Optional<MonitoredEndpoint> findById(Long endpointId) {
        return monitoredEndpointRepository.findById(endpointId);
    }

    public List<MonitoredEndpoint> findAll() {
        return monitoredEndpointRepository.findAll();
    }

    public MonitoredEndpoint authorizeAndFetchEndpoint(String accessToken, Long id, String message) {
        User user = userService.getUserByAccessToken(accessToken);
        MonitoredEndpoint endpoint = monitoredEndpointRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(messageSource.getMessage("entityNotFoundException.notFound", null, LocaleContextHolder.getLocale())));

        if (!endpoint.getOwner().getId().equals(user.getId())) {
            throw new ForbiddenException(message);
        }

        return endpoint;
    }

    private void updateEndpointWithPersistentData(User user, Long id, MonitoredEndpoint endpoint, MonitoredEndpoint existingEndpoint) {
        endpoint.setId(id);
        endpoint.setOwner(user);
        endpoint.setMonitoringResults(existingEndpoint.getMonitoringResults());
        endpoint.setDateOfCreation(existingEndpoint.getDateOfCreation());
        endpoint.setDateOfLastCheck(existingEndpoint.getDateOfLastCheck());
    }

    private void validateEndpoint(MonitoredEndpoint endpoint) {
        if (endpoint.getUrl() == null || endpoint.getUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("URL cannot be empty");
        }

        if (endpoint.getMonitoredInterval() == null || endpoint.getMonitoredInterval() < 1) {
            throw new IllegalArgumentException("Monitored interval must be greater or equal than 1 second");
        }
    }

}
