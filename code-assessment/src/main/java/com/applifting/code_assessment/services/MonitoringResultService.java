package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.MonitoringResult;
import com.applifting.code_assessment.repositories.MonitoringResultRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoringResultService {

    private final MonitoringResultRepository monitoringResultRepository;
    private final MonitoredEndpointService monitoredEndpointService;
    private final UserService userService;
    private final MessageSource messageSource;

    @Autowired
    public MonitoringResultService(MonitoringResultRepository monitoringResultRepository, MonitoredEndpointService monitoredEndpointService,
                                   UserService userService, MessageSource messageSource) {
        this.monitoringResultRepository = monitoringResultRepository;
        this.monitoredEndpointService = monitoredEndpointService;
        this.userService = userService;
        this.messageSource = messageSource;
    }

    public List<MonitoringResult> getLast10ResultsForUserAndEndpoint(String accessToken, Long endpointId) {
        MonitoredEndpoint endpoint = monitoredEndpointService.authorizeAndFetchEndpoint(accessToken, endpointId, messageSource.getMessage("forbidden.read", null, LocaleContextHolder.getLocale()));
        return monitoringResultRepository.findTop10ByMonitoredEndpointOrderByDateOfCheckDesc(endpoint);
    }

}