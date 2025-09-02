// TripCost.java
package com.smartcar.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trip_costs", uniqueConstraints = {
        @UniqueConstraint(name = "uk_trip_cost_start_end", columnNames = { "start_point", "end_point" })
})
public class TripCost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_point", nullable = false)
    @NotBlank(message = "Start point is required")
    private String startPoint;

    @Column(name = "end_point", nullable = false)
    @NotBlank(message = "End point is required")
    private String endPoint;

    @Column(name = "base_cost", nullable = false)
    @NotNull(message = "Base cost is required")
    private BigDecimal baseCost;

    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;

    @Column(name = "last_update_on")
    private LocalDateTime lastUpdateOn;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    public TripCost() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdateOn = LocalDateTime.now();
        this.isActive = true;
    }

    public TripCost(String startPoint, String endPoint, BigDecimal baseCost) {
        this();
        this.startPoint = startPoint;
        this.endPoint = endPoint;
        this.baseCost = baseCost;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStartPoint() {
        return startPoint;
    }

    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(BigDecimal baseCost) {
        this.baseCost = baseCost;
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

    @PreUpdate
    public void preUpdate() {
        this.lastUpdateOn = LocalDateTime.now();
    }
}