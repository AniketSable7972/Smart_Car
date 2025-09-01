package com.smartcar.monitoring.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartcar.monitoring.exception.AlertNotFoundException;
import com.smartcar.monitoring.model.Alert;
import com.smartcar.monitoring.model.Alert.AlertSeverity;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.model.Trip;
import com.smartcar.monitoring.repository.AlertRepository;

@Service
@Transactional
public class AlertService {
    
    @Autowired
    private AlertRepository alertRepository;
    
    // Create new alert
    public Alert createAlert(Car car, String type, String severity, String message) {
        return createAlert(car, null, type, severity, message);
    }
    
    // Create new alert with trip
    public Alert createAlert(Car car, Trip trip, String type, String severity, String message) {
        Alert alert = new Alert();
        alert.setCar(car);
        alert.setTrip(trip);
        alert.setType(type);
        alert.setSeverity(AlertSeverity.valueOf(severity.toUpperCase()));
        alert.setTimestamp(LocalDateTime.now());
        alert.setAcknowledged(false);
        alert.setCreationDate(LocalDateTime.now());
        alert.setLastUpdateOn(LocalDateTime.now());
        alert.setIsActive(true);
        
        return alertRepository.save(alert);
    }
    
    // Get alert by ID
    public Alert getAlertById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new AlertNotFoundException("Alert not found with ID: " + id));
    }
    
    // Get all active alerts
    public List<Alert> getAllActiveAlerts() {
        return alertRepository.findByIsActiveTrue();
    }
    
    // Get alerts by car
    public List<Alert> getAlertsByCar(Long carId) {
        return alertRepository.findByCarIdAndIsActiveTrue(carId);
    }
    
    // Get alerts by type
    public List<Alert> getAlertsByType(String type) {
        return alertRepository.findByTypeAndIsActiveTrue(type);
    }
    
    // Get alerts by severity
    public List<Alert> getAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverityAndIsActiveTrue(severity);
    }
    
    // Get unacknowledged alerts
    public List<Alert> getUnacknowledgedAlerts() {
        return alertRepository.findByAcknowledgedFalseAndIsActiveTrue();
    }
    
    // Get unacknowledged alerts by car
    public List<Alert> getUnacknowledgedAlertsByCar(Long carId) {
        return alertRepository.findByCarIdAndAcknowledgedFalseAndIsActiveTrue(carId);
    }
    
    // Get unacknowledged alerts by severity
    public List<Alert> getUnacknowledgedAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.findBySeverityAndAcknowledgedFalseAndIsActiveTrue(severity);
    }
    
    // Get alerts by timestamp range
    public List<Alert> getAlertsByTimestampRange(LocalDateTime startTime, LocalDateTime endTime) {
        return alertRepository.findByTimestampBetweenAndIsActiveTrue(startTime, endTime);
    }
    
    // Get alerts by car and timestamp range
    public List<Alert> getAlertsByCarAndTimestampRange(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        return alertRepository.findByCarIdAndTimestampBetweenAndIsActiveTrue(carId, startTime, endTime);
    }
    
    // Get critical alerts (HIGH and CRITICAL severity)
    public List<Alert> getCriticalAlerts() {
        return alertRepository.findCriticalAlerts();
    }
    
    // Get alerts by car and severity
    public List<Alert> getAlertsByCarAndSeverity(Long carId, AlertSeverity severity) {
        return alertRepository.findByCarIdAndSeverityAndIsActiveTrue(carId, severity);
    }
    
    // Get alerts by car and type
    public List<Alert> getAlertsByCarAndType(Long carId, String type) {
        return alertRepository.findByCarIdAndTypeAndIsActiveTrue(carId, type);
    }
    
    // Acknowledge alert
    public Alert acknowledgeAlert(Long id) {
        Alert alert = getAlertById(id);
        alert.setAcknowledged(true);
        alert.setLastUpdateOn(LocalDateTime.now());
        return alertRepository.save(alert);
    }
    
    // Acknowledge all active alerts
    public void acknowledgeAllAlerts() {
        List<Alert> alerts = alertRepository.findByAcknowledgedFalseAndIsActiveTrue();
        for (Alert alert : alerts) {
            alert.setAcknowledged(true);
            alert.setLastUpdateOn(LocalDateTime.now());
        }
        alertRepository.saveAll(alerts);
    }
    
    // Update alert
    public Alert updateAlert(Long id, Alert alertDetails) {
        Alert alert = getAlertById(id);
        
        alert.setType(alertDetails.getType());
        alert.setSeverity(alertDetails.getSeverity());
        alert.setAcknowledged(alertDetails.getAcknowledged());
        alert.setLastUpdateOn(LocalDateTime.now());
        
        return alertRepository.save(alert);
    }
    
    // Soft delete alert
    public void deactivateAlert(Long id) {
        Alert alert = getAlertById(id);
        alert.setIsActive(false);
        alert.setLastUpdateOn(LocalDateTime.now());
        alertRepository.save(alert);
    }
    
    // Reactivate alert
    public void reactivateAlert(Long id) {
        Alert alert = getAlertById(id);
        alert.setIsActive(true);
        alert.setLastUpdateOn(LocalDateTime.now());
        alertRepository.save(alert);
    }
    
    // Count alerts by car
    public long countAlertsByCar(Long carId) {
        return alertRepository.countByCarIdAndIsActiveTrue(carId);
    }
    
    // Count total active alerts
    public long countTotalActiveAlerts() {
        return alertRepository.countByIsActiveTrue();
    }
    
    // Count unacknowledged alerts
    public long countUnacknowledgedAlerts() {
        return alertRepository.countByAcknowledgedFalseAndIsActiveTrue();
    }
    
    // Count alerts by severity
    public long countAlertsBySeverity(AlertSeverity severity) {
        return alertRepository.countBySeverityAndIsActiveTrue(severity);
    }
    
    // Count alerts by type
    public long countAlertsByType(String type) {
        return alertRepository.countByTypeAndIsActiveTrue(type);
    }
    
    // Count alerts in time range
    public long countAlertsInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return alertRepository.countByTimestampBetweenAndIsActiveTrue(startTime, endTime);
    }
    
    // Count critical alerts
    public long countCriticalAlerts() {
        return alertRepository.countBySeverityInAndIsActiveTrue(List.of(AlertSeverity.CRITICAL));
    }
    
    // Get alerts created in date range
    public List<Alert> getAlertsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return alertRepository.findAlertsCreatedBetween(startDate, endDate);
    }
}
