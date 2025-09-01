package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.*;
import com.smartcar.monitoring.model.Alert;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.model.Trip;
import com.smartcar.monitoring.service.AlertService;
import com.smartcar.monitoring.service.CarService;
import com.smartcar.monitoring.service.TripService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/alerts")
@CrossOrigin(origins = "*")
public class AlertController {

    @Autowired
    private AlertService alertService;
    
    @Autowired
    private CarService carService;

    @Autowired
    private TripService tripService;

    // POST /api/alerts - Create new alert
    @PostMapping
    public ResponseEntity<ApiResponseDto<AlertDto>> createAlert(@Valid @RequestBody AlertDto alertDto) {
        try {
            Car car = carService.getCarById(alertDto.getCarId());
            String message = "Alert for " + alertDto.getType() + " with severity " + alertDto.getSeverity();
            
            Alert createdAlert = alertService.createAlert(car, alertDto.getType(), 
                                                        alertDto.getSeverity().toString(), message);
            AlertDto createdAlertDto = new AlertDto(createdAlert);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Alert created successfully", createdAlertDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to create alert: " + e.getMessage()));
        }
    }

    // GET /api/alerts - Get all active alerts
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getAllAlerts() {
        try {
            List<Alert> alerts = alertService.getAllActiveAlerts();
            List<AlertDto> alertDtos = alerts.stream()
                    .map(AlertDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Alerts retrieved successfully", alertDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve alerts: " + e.getMessage()));
        }
    }

    // GET /api/alerts/{id} - Get alert by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<AlertDto>> getAlertById(@PathVariable Long id) {
        try {
            Alert alert = alertService.getAlertById(id);
            AlertDto alertDto = new AlertDto(alert);
            return ResponseEntity.ok(ApiResponseDto.success("Alert retrieved successfully", alertDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Alert not found: " + e.getMessage()));
        }
    }

    // GET /api/alerts/car/{carId} - Get alerts by car ID
    @GetMapping("/car/{carId}")
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getAlertsByCar(@PathVariable Long carId) {
        try {
            List<Alert> alerts = alertService.getAlertsByCar(carId);
            List<AlertDto> alertDtos = alerts.stream()
                    .map(AlertDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Alerts retrieved successfully", alertDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve alerts: " + e.getMessage()));
        }
    }

    // GET /api/alerts/severity/{severity} - Get alerts by severity
    @GetMapping("/severity/{severity}")
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getAlertsBySeverity(@PathVariable String severity) {
        try {
            Alert.AlertSeverity alertSeverity = Alert.AlertSeverity.valueOf(severity.toUpperCase());
            List<Alert> alerts = alertService.getAlertsBySeverity(alertSeverity);
            List<AlertDto> alertDtos = alerts.stream()
                    .map(AlertDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Alerts retrieved successfully", alertDtos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Invalid severity: " + severity));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve alerts: " + e.getMessage()));
        }
    }

    // GET /api/alerts/type/{type} - Get alerts by type
    @GetMapping("/type/{type}")
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getAlertsByType(@PathVariable String type) {
        try {
            List<Alert> alerts = alertService.getAlertsByType(type);
            List<AlertDto> alertDtos = alerts.stream()
                    .map(AlertDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Alerts retrieved successfully", alertDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve alerts: " + e.getMessage()));
        }
    }

    // GET /api/alerts/unacknowledged - Get unacknowledged alerts
    @GetMapping("/unacknowledged")
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getUnacknowledgedAlerts() {
        try {
            List<Alert> alerts = alertService.getUnacknowledgedAlerts();
            List<AlertDto> alertDtos = alerts.stream()
                    .map(AlertDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Unacknowledged alerts retrieved successfully", alertDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve unacknowledged alerts: " + e.getMessage()));
        }
    }

    // GET /api/alerts/critical - Get critical alerts
    @GetMapping("/critical")
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getCriticalAlerts() {
        try {
            List<Alert> alerts = alertService.getCriticalAlerts();
            List<AlertDto> alertDtos = alerts.stream()
                    .map(AlertDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Critical alerts retrieved successfully", alertDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve critical alerts: " + e.getMessage()));
        }
    }

    // PUT /api/alerts/{id}/acknowledge - Acknowledge alert
    @PutMapping("/{id}/acknowledge")
    public ResponseEntity<ApiResponseDto<AlertDto>> acknowledgeAlert(@PathVariable Long id) {
        try {
            Alert alert = alertService.acknowledgeAlert(id);
            AlertDto alertDto = new AlertDto(alert);
            return ResponseEntity.ok(ApiResponseDto.success("Alert acknowledged successfully", alertDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to acknowledge alert: " + e.getMessage()));
        }
    }

    // PUT /api/alerts/acknowledge-all - Acknowledge all active alerts
    @PutMapping("/acknowledge-all")
    public ResponseEntity<ApiResponseDto<String>> acknowledgeAllAlerts() {
        try {
            alertService.acknowledgeAllAlerts();
            return ResponseEntity.ok(ApiResponseDto.success("All alerts acknowledged successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to acknowledge all alerts: " + e.getMessage()));
        }
    }

    // GET /api/alerts/stats/count - Get alert count statistics
    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponseDto<Object>> getAlertCountStats() {
        try {
            long totalAlerts = alertService.countTotalActiveAlerts();
            long unacknowledgedAlerts = alertService.countUnacknowledgedAlerts();
            long criticalAlerts = alertService.countCriticalAlerts();
            
            class AlertStats {
                public final long totalAlerts;
                public final long unacknowledgedAlerts;
                public final long criticalAlerts;
                public AlertStats(long totalAlerts, long unacknowledgedAlerts, long criticalAlerts) {
                    this.totalAlerts = totalAlerts;
                    this.unacknowledgedAlerts = unacknowledgedAlerts;
                    this.criticalAlerts = criticalAlerts;
                }
            }
            AlertStats stats = new AlertStats(totalAlerts, unacknowledgedAlerts, criticalAlerts);
            return ResponseEntity.ok(ApiResponseDto.success("Alert statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve alert statistics: " + e.getMessage()));
        }
    }

    // GET /api/alerts/stats/severity - Get alert statistics by severity
    @GetMapping("/stats/severity")
    public ResponseEntity<ApiResponseDto<Object>> getAlertSeverityStats() {
        try {
            long lowAlerts = alertService.countAlertsBySeverity(Alert.AlertSeverity.LOW);
            long mediumAlerts = alertService.countAlertsBySeverity(Alert.AlertSeverity.MEDIUM);
            long highAlerts = alertService.countAlertsBySeverity(Alert.AlertSeverity.HIGH);
            long criticalAlerts = alertService.countAlertsBySeverity(Alert.AlertSeverity.CRITICAL);
            
            class AlertSeverityStats {
                public final long lowAlerts;
                public final long mediumAlerts;
                public final long highAlerts;
                public final long criticalAlerts;
                public AlertSeverityStats(long lowAlerts, long mediumAlerts, long highAlerts, long criticalAlerts) {
                    this.lowAlerts = lowAlerts;
                    this.mediumAlerts = mediumAlerts;
                    this.highAlerts = highAlerts;
                    this.criticalAlerts = criticalAlerts;
                }
            }
            AlertSeverityStats stats = new AlertSeverityStats(lowAlerts, mediumAlerts, highAlerts, criticalAlerts);
            return ResponseEntity.ok(ApiResponseDto.success("Alert severity statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve alert severity statistics: " + e.getMessage()));
        }
    }

    // DELETE /api/alerts/{id} - Soft delete alert
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deactivateAlert(@PathVariable Long id) {
        try {
            alertService.deactivateAlert(id);
            return ResponseEntity.ok(ApiResponseDto.success("Alert deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to deactivate alert: " + e.getMessage()));
        }
    }

    // PUT /api/alerts/{id}/reactivate - Reactivate alert
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponseDto<String>> reactivateAlert(@PathVariable Long id) {
        try {
            alertService.reactivateAlert(id);
            return ResponseEntity.ok(ApiResponseDto.success("Alert reactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to reactivate alert: " + e.getMessage()));
        }
    }

    // GET /api/alerts/recent - Get recent alerts (last 24 hours)
    @GetMapping("/recent")
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getRecentAlerts() {
        try {
            LocalDateTime startTime = LocalDateTime.now().minusHours(24);
            List<Alert> alerts = alertService.getAlertsByTimestampRange(startTime, LocalDateTime.now());
            List<AlertDto> alertDtos = alerts.stream()
                    .map(AlertDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Recent alerts retrieved successfully", alertDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve recent alerts: " + e.getMessage()));
        }
    }

    // GET /api/alerts/trip/{tripId} - Get alerts for a trip based on trip timeframe
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<ApiResponseDto<List<AlertDto>>> getAlertsByTrip(@PathVariable Long tripId) {
        try {
            Trip trip = tripService.getById(tripId).orElseThrow(() -> new RuntimeException("Trip not found"));
            if (trip.getCar() == null) {
                return ResponseEntity.ok(ApiResponseDto.success("No car for trip", List.of()));
            }
            LocalDateTime start = trip.getStartedAt() != null ? trip.getStartedAt() : trip.getApprovedAt();
            LocalDateTime end = trip.getEndedAt() != null ? trip.getEndedAt() : LocalDateTime.now();
            List<Alert> alerts = alertService.getAlertsByCarAndTimestampRange(trip.getCar().getId(), start, end);
            List<AlertDto> alertDtos = alerts.stream().map(AlertDto::new).collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Trip alerts retrieved", alertDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve trip alerts: " + e.getMessage()));
        }
    }
}
