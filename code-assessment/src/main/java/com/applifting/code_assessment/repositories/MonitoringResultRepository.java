package com.applifting.code_assessment.repositories;

import com.applifting.code_assessment.domain.MonitoredEndpoint;
import com.applifting.code_assessment.domain.MonitoringResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MonitoringResultRepository extends JpaRepository<MonitoringResult, Long> {
    List<MonitoringResult> findTop10ByMonitoredEndpointOrderByDateOfCheckDesc(MonitoredEndpoint monitoredEndpoint);
}