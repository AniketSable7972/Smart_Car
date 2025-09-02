// TelemetryController.java
package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.*;
import com.smartcar.monitoring.model.Telemetry;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.service.TelemetryService;
import com.smartcar.monitoring.service.CarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/telemetry")
@CrossOrigin(origins = "*")
public class TelemetryController {

    @Autowired
    private TelemetryService telemetryService;

    @Autowired
    private CarService carService;

    // POST /api/telemetry - Create new telemetry record
    @PostMapping
    public ResponseEntity<ApiResponseDto<TelemetryDto>> createTelemetry(@Valid @RequestBody TelemetryDto telemetryDto) {
        try {
            Car car = carService.getCarById(telemetryDto.getCarId());
            Telemetry telemetry = new Telemetry();
            telemetry.setCar(car);
            telemetry.setSpeed(telemetryDto.getSpeed());
            telemetry.setFuel(telemetryDto.getFuelLevel());
            telemetry.setTemperature(telemetryDto.getTemperature());
            telemetry.setLocation(telemetryDto.getLocation());
            telemetry.setTimestamp(telemetryDto.getTimestamp());

            Telemetry createdTelemetry = telemetryService.createTelemetry(telemetry);
            TelemetryDto createdTelemetryDto = new TelemetryDto(createdTelemetry);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Telemetry created successfully", createdTelemetryDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to create telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry - Get all telemetry records
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<TelemetryDto>>> getAllTelemetry() {
        try {
            List<Telemetry> telemetryList = telemetryService.getAllActiveTelemetry();
            List<TelemetryDto> telemetryDtos = telemetryList.stream()
                    .map(TelemetryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry retrieved successfully", telemetryDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/{id} - Get telemetry by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<TelemetryDto>> getTelemetryById(@PathVariable Long id) {
        try {
            Telemetry telemetry = telemetryService.getTelemetryById(id);
            TelemetryDto telemetryDto = new TelemetryDto(telemetry);
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry retrieved successfully", telemetryDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Telemetry not found: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/car/{carId} - Get telemetry by car ID
    @GetMapping("/car/{carId}")
    public ResponseEntity<ApiResponseDto<List<TelemetryDto>>> getTelemetryByCar(@PathVariable Long carId) {
        try {
            List<Telemetry> telemetryList = telemetryService.getTelemetryByCar(carId);
            List<TelemetryDto> telemetryDtos = telemetryList.stream()
                    .map(TelemetryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry retrieved successfully", telemetryDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/car/{carId}/latest - Get latest telemetry for car
    @GetMapping("/car/{carId}/latest")
    public ResponseEntity<ApiResponseDto<List<TelemetryDto>>> getLatestTelemetryByCar(@PathVariable Long carId) {
        try {
            List<Telemetry> telemetryList = telemetryService.getLatestTelemetryByCar(carId);
            List<TelemetryDto> telemetryDtos = telemetryList.stream()
                    .map(TelemetryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Latest telemetry retrieved successfully", telemetryDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Latest telemetry not found: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/car/{carId}/range - Get telemetry by car ID and time range
    @GetMapping("/car/{carId}/range")
    public ResponseEntity<ApiResponseDto<List<TelemetryDto>>> getTelemetryByCarAndTimeRange(
            @PathVariable Long carId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        try {
            List<Telemetry> telemetryList = telemetryService.getTelemetryByCarAndTimestampRange(carId, startTime,
                    endTime);
            List<TelemetryDto> telemetryDtos = telemetryList.stream()
                    .map(TelemetryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry retrieved successfully", telemetryDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/latest/all - Get latest telemetry for all cars
    @GetMapping("/latest/all")
    public ResponseEntity<ApiResponseDto<List<TelemetryDto>>> getLatestTelemetryForAllCars() {
        try {
            List<Telemetry> telemetryList = telemetryService.getLatestTelemetryForAllCars();
            List<TelemetryDto> telemetryDtos = telemetryList.stream()
                    .map(TelemetryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity
                    .ok(ApiResponseDto.success("Latest telemetry for all cars retrieved successfully", telemetryDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve latest telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/stats/car/{carId} - Get telemetry statistics for car
    @GetMapping("/stats/car/{carId}")
    public ResponseEntity<ApiResponseDto<Object>> getTelemetryStatsByCar(@PathVariable Long carId,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime) {
        try {
            if (startTime == null)
                startTime = LocalDateTime.now().minusDays(7);
            if (endTime == null)
                endTime = LocalDateTime.now();

            TelemetryService.TelemetryStatistics stats = telemetryService.getTelemetryStatistics(carId, startTime,
                    endTime);
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve telemetry statistics: " + e.getMessage()));
        }
    }

    // DELETE /api/telemetry/{id} - Soft delete telemetry record
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deactivateTelemetry(@PathVariable Long id) {
        try {
            telemetryService.deactivateTelemetry(id);
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to deactivate telemetry: " + e.getMessage()));
        }
    }

    // PUT /api/telemetry/{id}/reactivate - Reactivate telemetry record
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponseDto<String>> reactivateTelemetry(@PathVariable Long id) {
        try {
            telemetryService.reactivateTelemetry(id);
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry reactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to reactivate telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/trip/{tripId} - Get telemetry by trip ID
    @GetMapping("/trip/{tripId}")
    public ResponseEntity<ApiResponseDto<List<TelemetryDto>>> getTelemetryByTrip(@PathVariable Long tripId) {
        try {
            List<Telemetry> telemetryList = telemetryService.getTelemetryByTrip(tripId);
            List<TelemetryDto> telemetryDtos = telemetryList.stream()
                    .map(TelemetryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry retrieved successfully", telemetryDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/trip/{tripId}/range - Get telemetry by trip ID and time
    // range
    @GetMapping("/trip/{tripId}/range")
    public ResponseEntity<ApiResponseDto<List<TelemetryDto>>> getTelemetryByTripAndTimeRange(
            @PathVariable Long tripId,
            @RequestParam LocalDateTime startTime,
            @RequestParam LocalDateTime endTime) {
        try {
            List<Telemetry> telemetryList = telemetryService.getTelemetryByTripAndTimestampRange(tripId, startTime,
                    endTime);
            List<TelemetryDto> telemetryDtos = telemetryList.stream()
                    .map(TelemetryDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry retrieved successfully", telemetryDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve telemetry: " + e.getMessage()));
        }
    }

    // GET /api/telemetry/stats/trip/{tripId} - Get telemetry statistics for trip
    @GetMapping("/stats/trip/{tripId}")
    public ResponseEntity<ApiResponseDto<Object>> getTelemetryStatsByTrip(@PathVariable Long tripId) {
        try {
            TelemetryService.TelemetryStatistics stats = telemetryService.getTelemetryStatisticsByTrip(tripId);
            return ResponseEntity.ok(ApiResponseDto.success("Telemetry trip statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve telemetry trip statistics: " + e.getMessage()));
        }
    }
}
