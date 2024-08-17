package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.MonitoringResult;
import com.applifting.code_assessment.domain.User;
import com.applifting.code_assessment.repositories.MonitoringResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MonitoringResultService {

    private final MonitoringResultRepository monitoringResultRepository;
    private final MonitoredEndpointService monitoredEndpointService;
    private final UserService userService;

    @Autowired
    public MonitoringResultService(MonitoringResultRepository monitoringResultRepository, MonitoredEndpointService monitoredEndpointService, UserService userService) {
        this.monitoringResultRepository = monitoringResultRepository;
        this.monitoredEndpointService = monitoredEndpointService;
        this.userService = userService;
    }

    public Optional<List<MonitoringResult>> getLast10ResultsForUserAndEndpoint(String accessToken, Long endpointId) {
        User user = userService.getUserByAccessToken(accessToken);
        Optional<MonitoredEndpoint> endpoint = monitoredEndpointService.findById(endpointId);
        if (endpoint.isPresent() && endpoint.get().getOwner().getId().equals(user.getId())) {
            return Optional.of(monitoringResultRepository.findTop10ByMonitoredEndpointOrderByDateOfCheckDesc(endpoint.get()));
        }
        return Optional.empty();
    }

}