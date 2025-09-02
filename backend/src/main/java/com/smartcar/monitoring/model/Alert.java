// Alert.java
package com.smartcar.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
public class Alert {

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
    @NotBlank(message = "Alert type is required")
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Severity is required")
    private AlertSeverity severity;

    @Column(nullable = false)
    @NotNull(message = "Timestamp is required")
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private Boolean acknowledged = false;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "last_update_on")
    private LocalDateTime lastUpdateOn;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    // Enums
    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Constructors
    public Alert() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdateOn = LocalDateTime.now();
        this.isActive = true;
        this.timestamp = LocalDateTime.now();
        this.acknowledged = false;
    }

    public Alert(Car car, String type, AlertSeverity severity) {
        this();
        this.car = car;
        this.type = type;
        this.severity = severity;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public AlertSeverity getSeverity() {
        return severity;
    }

    public void setSeverity(AlertSeverity severity) {
        this.severity = severity;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getAcknowledged() {
        return acknowledged;
    }

    public void setAcknowledged(Boolean acknowledged) {
        this.acknowledged = acknowledged;
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
        return "Alert{" +
                "id=" + id +
                ", carId=" + (car != null ? car.getId() : null) +
                ", tripId=" + (trip != null ? trip.getId() : null) +
                ", type='" + type + '\'' +
                ", severity=" + severity +
                ", timestamp=" + timestamp +
                ", acknowledged=" + acknowledged +
                ", isActive=" + isActive +
                '}';
    }
}
