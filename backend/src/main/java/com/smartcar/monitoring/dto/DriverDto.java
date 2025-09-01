package com.smartcar.monitoring.dto;

import com.smartcar.monitoring.model.Driver;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class DriverDto {
    private Long id;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String username;
    private String name;
    private String email;
    
    private Long assignedCarId;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateOn;
    private Boolean isActive;
    
    // Default constructor
    public DriverDto() {}
    
    // Constructor from Driver entity
    public DriverDto(Driver driver) {
        this.id = driver.getId();
        this.userId = driver.getUser().getId();
        this.assignedCarId = driver.getAssignedCarId();
        this.creationDate = driver.getCreationDate();
        this.lastUpdateOn = driver.getLastUpdateOn();
        this.isActive = driver.getIsActive();
        
        if (driver.getUser() != null) {
            this.username = driver.getUser().getUsername();
            this.name = driver.getUser().getName();
            this.email = driver.getUser().getEmail();
        }
    }
    
    // Constructor for creation
    public DriverDto(Long userId, Long assignedCarId) {
        this.userId = userId;
        this.assignedCarId = assignedCarId;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Long getAssignedCarId() { return assignedCarId; }
    public void setAssignedCarId(Long assignedCarId) { this.assignedCarId = assignedCarId; }
    
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    
    public LocalDateTime getLastUpdateOn() { return lastUpdateOn; }
    public void setLastUpdateOn(LocalDateTime lastUpdateOn) { this.lastUpdateOn = lastUpdateOn; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
