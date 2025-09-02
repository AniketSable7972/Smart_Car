// TripRequestDto.java
package com.smartcar.monitoring.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TripRequestDto {
    @NotNull(message = "Driver ID is required")
    private Long driverId;

    private Long carId; // optional, admin may assign

    @NotBlank(message = "Start point is required")
    private String startPoint;

    @NotBlank(message = "End point is required")
    private String endPoint;

    public Long getDriverId() {
        return driverId;
    }

    public void setDriverId(Long driverId) {
        this.driverId = driverId;
    }

    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
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
}