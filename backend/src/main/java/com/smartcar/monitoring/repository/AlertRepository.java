package com.smartcar.monitoring.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartcar.monitoring.model.Alert;
import com.smartcar.monitoring.model.Alert.AlertSeverity;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    
    // Find by car ID
    List<Alert> findByCarIdAndIsActiveTrue(Long carId);
    
    // Find by alert type
    List<Alert> findByTypeAndIsActiveTrue(String type);
    
    // Find by severity
    List<Alert> findBySeverityAndIsActiveTrue(AlertSeverity severity);
    
    // Find by acknowledgment status
    List<Alert> findByAcknowledgedAndIsActiveTrue(Boolean acknowledged);
    
    // Find by timestamp range
    List<Alert> findByTimestampBetweenAndIsActiveTrue(LocalDateTime startTime, LocalDateTime endTime);
    
    // Find by car ID and timestamp range
    List<Alert> findByCarIdAndTimestampBetweenAndIsActiveTrue(Long carId, LocalDateTime startTime, LocalDateTime endTime);
    
    // Find unacknowledged alerts
    List<Alert> findByAcknowledgedFalseAndIsActiveTrue();
    
    // Find unacknowledged alerts by car
    List<Alert> findByCarIdAndAcknowledgedFalseAndIsActiveTrue(Long carId);
    
    // Find unacknowledged alerts by severity
    List<Alert> findBySeverityAndAcknowledgedFalseAndIsActiveTrue(AlertSeverity severity);
    
    // Find all active alerts
    List<Alert> findByIsActiveTrue();
    
    // Find alerts created in date range
    @Query("SELECT a FROM Alert a WHERE a.creationDate BETWEEN :startDate AND :endDate AND a.isActive = true")
    List<Alert> findAlertsCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                        @Param("endDate") LocalDateTime endDate);
    
    // Find critical alerts (HIGH and CRITICAL severity)
    @Query("SELECT a FROM Alert a WHERE a.severity IN ('HIGH', 'CRITICAL') AND a.isActive = true")
    List<Alert> findCriticalAlerts();
    
    // Find alerts by car and severity
    List<Alert> findByCarIdAndSeverityAndIsActiveTrue(Long carId, AlertSeverity severity);
    
    // Find alerts by car and type
    List<Alert> findByCarIdAndTypeAndIsActiveTrue(Long carId, String type);
    
    // Count alerts by car
    long countByCarIdAndIsActiveTrue(Long carId);
    
    // Count total active alerts
    long countByIsActiveTrue();
    
    // Count unacknowledged alerts
    long countByAcknowledgedFalseAndIsActiveTrue();
    
    // Count alerts by severity
    long countBySeverityAndIsActiveTrue(AlertSeverity severity);
    
    // Count alerts by type
    long countByTypeAndIsActiveTrue(String type);
    
    // Count alerts in time range
    long countByTimestampBetweenAndIsActiveTrue(LocalDateTime startTime, LocalDateTime endTime);
    
    // Count critical alerts
    long countBySeverityInAndIsActiveTrue(List<AlertSeverity> severities);
}
