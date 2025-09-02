// TelemetryDto.java
package com.smartcar.monitoring.dto;

import com.smartcar.monitoring.model.Telemetry;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class TelemetryDto {
    private Long id;

    @NotNull(message = "Car ID is required")
    private Long carId;

    private Long tripId;

    @NotNull(message = "Speed is required")
    @Min(value = 0, message = "Speed cannot be negative")
    @Max(value = 220, message = "Speed cannot exceed 220 km/h")
    private Integer speed;

    @NotNull(message = "Fuel level is required")
    @Min(value = 0, message = "Fuel level cannot be negative")
    @Max(value = 100, message = "Fuel level cannot exceed 100%")
    private Integer fuelLevel;

    @NotNull(message = "Temperature is required")
    @Min(value = -20, message = "Temperature cannot be below -20°C")
    @Max(value = 130, message = "Temperature cannot exceed 130°C")
    private Integer temperature;

    // Location optional for simulator messages
    // @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateOn;
    private Boolean isActive;

    // Default constructor
    public TelemetryDto() {
    }

    // Constructor from Telemetry entity
    public TelemetryDto(Telemetry telemetry) {
        this.id = telemetry.getId();
        this.carId = telemetry.getCar().getId();
        this.tripId = telemetry.getTrip() != null ? telemetry.getTrip().getId() : null;
        this.speed = telemetry.getSpeed();
        this.fuelLevel = telemetry.getFuel();
        this.temperature = telemetry.getTemperature();
        this.location = telemetry.getLocation();
        this.timestamp = telemetry.getTimestamp();
        this.creationDate = telemetry.getCreationDate();
        this.lastUpdateOn = telemetry.getLastUpdateOn();
        this.isActive = telemetry.getIsActive();
    }

    // Constructor for creation
    public TelemetryDto(Long carId, Long tripId, Integer speed, Integer fuelLevel, Integer temperature,
            String location, LocalDateTime timestamp) {
        this.carId = carId;
        this.tripId = tripId;
        this.speed = speed;
        this.fuelLevel = fuelLevel;
        this.temperature = temperature;
        this.location = location;
        this.timestamp = timestamp;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public Long getTripId() {
        return tripId;
    }

    public void setTripId(Long tripId) {
        this.tripId = tripId;
    }

    public Integer getSpeed() {
        return speed;
    }

    public void setSpeed(Integer speed) {
        this.speed = speed;
    }

    public Integer getFuelLevel() {
        return fuelLevel;
    }

    public void setFuelLevel(Integer fuelLevel) {
        this.fuelLevel = fuelLevel;
    }

    public Integer getTemperature() {
        return temperature;
    }

    public void setTemperature(Integer temperature) {
        this.temperature = temperature;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public LocalDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public LocalDateTime getLastUpdateOn() {
        return lastUpdateOn;
    }

    public void setLastUpdateOn(LocalDateTime lastUpdateOn) {
        this.lastUpdateOn = lastUpdateOn;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
