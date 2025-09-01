package com.smartcar.monitoring.dto;

import com.smartcar.monitoring.model.Car;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class CarDto {
    private Long id;
    
    @NotBlank(message = "Status is required")
    private String status;
    
    @NotBlank(message = "Car number is required")
    private String carNumber;
    
    @NotBlank(message = "Car model is required")
    private String carModel;
    
    @NotNull(message = "Speed is required")
    @Min(value = 0, message = "Speed cannot be negative")
    @Max(value = 200, message = "Speed cannot exceed 200 km/h")
    private Integer speed;
    
    @NotNull(message = "Fuel level is required")
    @Min(value = 0, message = "Fuel level cannot be negative")
    @Max(value = 100, message = "Fuel level cannot exceed 100%")
    private Integer fuelLevel;
    
    @NotNull(message = "Temperature is required")
    @Min(value = -20, message = "Temperature cannot be below -20°C")
    @Max(value = 60, message = "Temperature cannot exceed 60°C")
    private Integer temperature;
    
    @NotBlank(message = "Location is required")
    private String location;
    
    private Long driverId;
    private String driverName;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateOn;
    private Boolean isActive;
    
    // Default constructor
    public CarDto() {}
    
    // Constructor from Car entity
    public CarDto(Car car) {
        this.id = car.getId();
        this.status = car.getStatus();
        this.carNumber = car.getCarNumber();
        this.carModel = car.getCarModel();
        this.speed = car.getSpeed();
        this.fuelLevel = car.getFuelLevel();
        this.temperature = car.getTemperature();
        this.location = car.getLocation();
        this.creationDate = car.getCreationDate();
        this.lastUpdateOn = car.getLastUpdateOn();
        this.isActive = car.getIsActive();
        
        if (car.getDriver() != null) {
            this.driverId = car.getDriver().getId();
            this.driverName = car.getDriver().getUser().getName();
        }
    }
    
    // Constructor for creation
    public CarDto(String status, String carNumber, String carModel, Integer speed, Integer fuelLevel, Integer temperature, String location) {
        this.status = status;
        this.carNumber = carNumber;
        this.carModel = carModel;
        this.speed = speed;
        this.fuelLevel = fuelLevel;
        this.temperature = temperature;
        this.location = location;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getCarNumber() { return carNumber; }
    public void setCarNumber(String carNumber) { this.carNumber = carNumber; }

    public String getCarModel() { return carModel; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    
    public Integer getSpeed() { return speed; }
    public void setSpeed(Integer speed) { this.speed = speed; }
    
    public Integer getFuelLevel() { return fuelLevel; }
    public void setFuelLevel(Integer fuelLevel) { this.fuelLevel = fuelLevel; }
    
    public Integer getTemperature() { return temperature; }
    public void setTemperature(Integer temperature) { this.temperature = temperature; }
    
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    
    public Long getDriverId() { return driverId; }
    public void setDriverId(Long driverId) { this.driverId = driverId; }
    
    public String getDriverName() { return driverName; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    
    public LocalDateTime getLastUpdateOn() { return lastUpdateOn; }
    public void setLastUpdateOn(LocalDateTime lastUpdateOn) { this.lastUpdateOn = lastUpdateOn; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
