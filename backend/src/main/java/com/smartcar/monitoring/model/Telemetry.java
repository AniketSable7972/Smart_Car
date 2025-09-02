// Telemetry.java
package com.smartcar.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "telemetry")
public class Telemetry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "car_id", nullable = false)
	@NotNull(message = "Car is required")
	private Car car;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "trip_id")
	private Trip trip;

	@Column(nullable = false)
	@NotNull(message = "Timestamp is required")
	private LocalDateTime timestamp;

	@Column(nullable = false)
	@Min(value = 0, message = "Speed cannot be negative")
	@Max(value = 220, message = "Speed cannot exceed 220 km/h")
	private Integer speed;

	@Column(nullable = false)
	@Min(value = 0, message = "Fuel cannot be negative")
	@Max(value = 100, message = "Fuel cannot exceed 100%")
	private Integer fuel;

	@Column(nullable = false)
	@Min(value = -20, message = "Temperature cannot be below -20°C")
	@Max(value = 130, message = "Temperature cannot exceed 130°C")
	private Integer temperature;

	@Column(nullable = false)
	// Location can be empty for simulator-generated messages
	private String location;

	@Column(name = "creation_date", nullable = false)
	private LocalDateTime creationDate;

	@Column(name = "last_update_on")
	private LocalDateTime lastUpdateOn;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive = true;

	// Constructors
	public Telemetry() {
		this.creationDate = LocalDateTime.now();
		this.lastUpdateOn = LocalDateTime.now();
		this.isActive = true;
		this.timestamp = LocalDateTime.now();
	}

	public Telemetry(Car car, Integer speed, Integer fuel, Integer temperature, String location) {
		this();
		this.car = car;
		this.speed = speed;
		this.fuel = fuel;
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

	public Car getCar() {
		return car;
	}

	public void setCar(Car car) {
		this.car = car;
	}

	public Trip getTrip() {
		return trip;
	}

	public void setTrip(Trip trip) {
		this.trip = trip;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(LocalDateTime timestamp) {
		this.timestamp = timestamp;
	}

	public Integer getSpeed() {
		return speed;
	}

	public void setSpeed(Integer speed) {
		this.speed = speed;
	}

	public Integer getFuel() {
		return fuel;
	}

	public void setFuel(Integer fuel) {
		this.fuel = fuel;
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
		return "Telemetry{" +
				"id=" + id +
				", carId=" + (car != null ? car.getId() : null) +
				", tripId=" + (trip != null ? trip.getId() : null) +
				", timestamp=" + timestamp +
				", speed=" + speed +
				", fuel=" + fuel +
				", temperature=" + temperature +
				", location='" + location + '\'' +
				", isActive=" + isActive +
				'}';
	}
}
