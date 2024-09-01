package com.applifting.code_assessment.services;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.User;
import com.applifting.code_assessment.exceptions.ForbiddenException;
import com.applifting.code_assessment.repositories.MonitoredEndpointRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class MonitoredEndpointServiceTest {

    @Mock
    private MonitoredEndpointRepository monitoredEndpointRepository;

    @Mock
    private UserService userService;

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private MessageSource messageSource;

    @InjectMocks
    private MonitoredEndpointService monitoredEndpointService;

    private User user;
    private MonitoredEndpoint endpoint;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);

        endpoint = new MonitoredEndpoint();
        endpoint.setId(1L);
        endpoint.setName("Test Endpoint");
        endpoint.setUrl("https://example.com");
        endpoint.setMonitoredInterval(60);
        endpoint.setOwner(user);
        endpoint.setDateOfCreation(LocalDateTime.now());
    }

    @Test
    void testCreateMonitoredEndpoint() {
        when(userService.getUserByAccessToken(anyString())).thenReturn(user);
        when(monitoredEndpointRepository.save(any(MonitoredEndpoint.class))).thenReturn(endpoint);

        MonitoredEndpoint createdEndpoint = monitoredEndpointService.createMonitoredEndpoint("accessToken", endpoint);

        assertNotNull(createdEndpoint);
        assertEquals("Test Endpoint", createdEndpoint.getName());
        verify(monitoredEndpointRepository, times(1)).save(endpoint);
        verify(monitoringService, times(1)).scheduleEndpointMonitoring(endpoint);
    }

    @Test
    void testUpdateMonitoredEndpoint_withInvalidUser_throwsForbiddenException() {
        when(monitoredEndpointRepository.findById(anyLong())).thenReturn(Optional.of(endpoint));
        when(userService.getUserByAccessToken(anyString())).thenReturn(new User());
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("message");

        assertThrows(ForbiddenException.class, () -> monitoredEndpointService.updateMonitoredEndpoint("accessToken", 1L, endpoint));
    }

    @Test
    void testDeleteMonitoredEndpoint_withValidUser() {
        when(monitoredEndpointRepository.findById(anyLong())).thenReturn(Optional.of(endpoint));
        when(userService.getUserByAccessToken(anyString())).thenReturn(user);
        when(messageSource.getMessage(anyString(), any(), any())).thenReturn("message");

        assertDoesNotThrow(() -> monitoredEndpointService.deleteMonitoredEndpoint("accessToken", 1L));

        verify(monitoredEndpointRepository, times(1)).deleteById(1L);
        verify(monitoringService, times(1)).stopMonitoring(1L);
    }

    @Test
    void testValidateEndpoint_withInvalidData_throwsIllegalArgumentException() {
        MonitoredEndpoint invalidEndpoint = new MonitoredEndpoint();
        invalidEndpoint.setName("Invalid Endpoint");

        assertThrows(IllegalArgumentException.class, () -> monitoredEndpointService.createMonitoredEndpoint("accessToken", invalidEndpoint));
    }
}
