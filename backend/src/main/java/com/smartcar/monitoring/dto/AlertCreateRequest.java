package com.smartcar.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;

public class AlertCreateRequest {
    
    @NotNull(message = "Car ID is required")
    private Long carId;
    
    @NotBlank(message = "Alert type is required")
    @Pattern(regexp = "^(ENGINE|FUEL|TEMPERATURE|SPEED|MAINTENANCE|SAFETY|OTHER)$", 
             message = "Alert type must be one of: ENGINE, FUEL, TEMPERATURE, SPEED, MAINTENANCE, SAFETY, OTHER")
    private String type;
    
    @NotBlank(message = "Alert severity is required")
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|CRITICAL)$", 
             message = "Alert severity must be one of: LOW, MEDIUM, HIGH, CRITICAL")
    private String severity;
    
    @NotBlank(message = "Alert message is required")
    private String message;
    
    private LocalDateTime timestamp;
    
    // Default constructor
    public AlertCreateRequest() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor with all fields
    public AlertCreateRequest(Long carId, String type, String severity, String message) {
        this.carId = carId;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
    
    // Constructor with timestamp
    public AlertCreateRequest(Long carId, String type, String severity, String message, LocalDateTime timestamp) {
        this.carId = carId;
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getCarId() {
        return carId;
    }
    
    public void setCarId(Long carId) {
        this.carId = carId;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getSeverity() {
        return severity;
    }
    
    public void setSeverity(String severity) {
        this.severity = severity;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
}
