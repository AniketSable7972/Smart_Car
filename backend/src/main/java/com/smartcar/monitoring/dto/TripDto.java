// TripDto.java
package com.smartcar.monitoring.dto;

import com.smartcar.monitoring.model.Trip;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TripDto {
    private Long id;
    private Long driverId;
    private String driverName;
    private Long carId;
    private String carModel;
    private String carNumber;
    private String startPoint;
    private String endPoint;
    private String status;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private BigDecimal baseCost;
    private BigDecimal additionalFine;
    private BigDecimal totalCost;

    public TripDto() {
    }

    public TripDto(Trip trip) {
        this.id = trip.getId();
        if (trip.getDriver() != null) {
            this.driverId = trip.getDriver().getId();
            this.driverName = trip.getDriver().getUser() != null
                    ? trip.getDriver().getUser().getName() // ✅ get driver’s name
                    : null;
        }
        if (trip.getCar() != null) {
            this.carId = trip.getCar().getId();
            this.carModel = trip.getCar().getCarModel(); // ✅ car model
            this.carNumber = trip.getCar().getCarNumber(); // ✅ car number
        }
        this.startPoint = trip.getStartPoint();
        this.endPoint = trip.getEndPoint();
        this.status = trip.getStatus() != null ? trip.getStatus().name() : null;
        this.requestedAt = trip.getRequestedAt();
        this.approvedAt = trip.getApprovedAt();
        this.startedAt = trip.getStartedAt();
        this.endedAt = trip.getEndedAt();
        this.baseCost = trip.getBaseCost();
        this.additionalFine = trip.getAdditionalFine();
        this.totalCost = trip.getTotalCost();
    }

    // ---------- Getters & Setters ----------
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public String getCarModel() {
        return carModel;
    }

    public void setCarModel(String carModel) {
        this.carModel = carModel;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getEndedAt() {
        return endedAt;
    }

    public void setEndedAt(LocalDateTime endedAt) {
        this.endedAt = endedAt;
    }

    public BigDecimal getBaseCost() {
        return baseCost;
    }

    public void setBaseCost(BigDecimal baseCost) {
        this.baseCost = baseCost;
    }

    public BigDecimal getAdditionalFine() {
        return additionalFine;
    }

    public void setAdditionalFine(BigDecimal additionalFine) {
        this.additionalFine = additionalFine;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }
}
