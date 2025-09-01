package com.smartcar.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cars")
public class Car {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "driver_id")
	private Driver driver;
	
	@Column(name = "car_number", nullable = false)
	@NotBlank(message = "Car number is required")
	private String carNumber;
	
	@Column(name = "car_model", nullable = false)
	@NotBlank(message = "Car model is required")
	private String carModel;
	
	@Column(nullable = false)
	@NotBlank(message = "Status is required")
	private String status;
	
	@Column(nullable = false)
	@Min(value = 0, message = "Speed cannot be negative")
	@Max(value = 200, message = "Speed cannot exceed 200 km/h")
	private Integer speed;
	
	@Column(name = "fuel_level", nullable = false)
	@Min(value = 0, message = "Fuel level cannot be negative")
	@Max(value = 100, message = "Fuel level cannot exceed 100%")
	private Integer fuelLevel;
	
	@Column(nullable = false)
	@Min(value = -20, message = "Temperature cannot be below -20°C")
	@Max(value = 60, message = "Temperature cannot exceed 60°C")
	private Integer temperature;
	
	@Column(nullable = false)
	@NotBlank(message = "Location is required")
	private String location;
	
	@Column(name = "creation_date", nullable = false)
	private LocalDateTime creationDate;
	
	@Column(name = "last_update_on")
	private LocalDateTime lastUpdateOn;
	
	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;
	
	// Constructors
	public Car() {
		this.creationDate = LocalDateTime.now();
		this.lastUpdateOn = LocalDateTime.now();
		this.isActive = true;
		this.speed = 0;
		this.fuelLevel = 100;
		this.temperature = 25;
		this.status = "IDLE";
		this.carNumber = "UNKNOWN";
		this.carModel = "UNKNOWN";
	}
	
	public Car(String status, Integer speed, Integer fuelLevel, Integer temperature, String location) {
		this();
		this.status = status;
		this.speed = speed;
		this.fuelLevel = fuelLevel;
		this.temperature = temperature;
		this.location = location;
	}
	
	// Getters and Setters
	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public Driver getDriver() {
		return driver;
	}
	
	public void setDriver(Driver driver) {
		this.driver = driver;
	}
	
	public String getCarNumber() { return carNumber; }
	public void setCarNumber(String carNumber) { this.carNumber = carNumber; }
	
	public String getCarModel() { return carModel; }
	public void setCarModel(String carModel) { this.carModel = carModel; }
	
	public String getStatus() {
		return status;
	}
	
	public void setStatus(String status) {
		this.status = status;
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
	
	// Pre-update hook
	@PreUpdate
	public void preUpdate() {
		this.lastUpdateOn = LocalDateTime.now();
	}
	
	@Override
	public String toString() {
		return "Car{" +
				"id=" + id +
				", driverId=" + (driver != null ? driver.getId() : null) +
				", carNumber='" + carNumber + '\'' +
				", carModel='" + carModel + '\'' +
				", status='" + status + '\'' +
				", speed=" + speed +
				", fuelLevel=" + fuelLevel +
				", temperature=" + temperature +
				", location='" + location + '\'' +
				", isActive=" + isActive +
				'}';
	}
}
