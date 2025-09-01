package com.smartcar.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trips")
public class Trip {
    
    public enum TripStatus {
        REQUESTED, APPROVED, REJECTED, ACTIVE, COMPLETED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    @NotNull(message = "Driver is required")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "car_id")
    private Car car;

    @Column(name = "start_point", nullable = false)
    @NotBlank(message = "Start point is required")
    private String startPoint;

    @Column(name = "end_point", nullable = false)
    @NotBlank(message = "End point is required")
    private String endPoint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status = TripStatus.REQUESTED;

    @Column(name = "requested_at", nullable = false)
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "base_cost", nullable = false)
    private BigDecimal baseCost = BigDecimal.ZERO;

    @Column(name = "additional_fine", nullable = false)
    private BigDecimal additionalFine = BigDecimal.ZERO;

    @Column(name = "total_cost", nullable = false)
    private BigDecimal totalCost = BigDecimal.ZERO;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "last_update_on")
    private LocalDateTime lastUpdateOn;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public Trip() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdateOn = LocalDateTime.now();
        this.isActive = true;
        this.requestedAt = LocalDateTime.now();
        this.status = TripStatus.REQUESTED;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Driver getDriver() { return driver; }
    public void setDriver(Driver driver) { this.driver = driver; }

    public Car getCar() { return car; }
    public void setCar(Car car) { this.car = car; }

    public String getStartPoint() { return startPoint; }
    public void setStartPoint(String startPoint) { this.startPoint = startPoint; }

    public String getEndPoint() { return endPoint; }
    public void setEndPoint(String endPoint) { this.endPoint = endPoint; }

    public TripStatus getStatus() { return status; }
    public void setStatus(TripStatus status) { this.status = status; }

    public LocalDateTime getRequestedAt() { return requestedAt; }
    public void setRequestedAt(LocalDateTime requestedAt) { this.requestedAt = requestedAt; }

    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getEndedAt() { return endedAt; }
    public void setEndedAt(LocalDateTime endedAt) { this.endedAt = endedAt; }

    public BigDecimal getBaseCost() { return baseCost; }
    public void setBaseCost(BigDecimal baseCost) { this.baseCost = baseCost; }

    public BigDecimal getAdditionalFine() { return additionalFine; }
    public void setAdditionalFine(BigDecimal additionalFine) { this.additionalFine = additionalFine; }

    public BigDecimal getTotalCost() { return totalCost; }
    public void setTotalCost(BigDecimal totalCost) { this.totalCost = totalCost; }

    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }

    public LocalDateTime getLastUpdateOn() { return lastUpdateOn; }
    public void setLastUpdateOn(LocalDateTime lastUpdateOn) { this.lastUpdateOn = lastUpdateOn; }

    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }

    @PreUpdate
    public void preUpdate() { this.lastUpdateOn = LocalDateTime.now(); }

    @Override
    public String toString() {
        return "Trip{" +
                "id=" + id +
                ", driverId=" + (driver != null ? driver.getId() : null) +
                ", carId=" + (car != null ? car.getId() : null) +
                ", startPoint='" + startPoint + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", status=" + status +
                ", totalCost=" + totalCost +
                '}';
    }
}