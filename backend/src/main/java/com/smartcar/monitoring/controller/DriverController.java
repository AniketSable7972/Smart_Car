package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.*;
import com.smartcar.monitoring.model.Driver;
import com.smartcar.monitoring.model.User;
import com.smartcar.monitoring.service.DriverService;
import com.smartcar.monitoring.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drivers")
@CrossOrigin(origins = "*")
public class DriverController {

    @Autowired
    private DriverService driverService;
    
    @Autowired
    private UserService userService;

    // POST /api/drivers - Create new driver
    @PostMapping
    public ResponseEntity<ApiResponseDto<DriverDto>> createDriver(@Valid @RequestBody DriverDto driverDto) {
        try {
            User user = userService.getUserById(driverDto.getUserId());
            Driver driver = new Driver();
            driver.setUser(user);
            driver.setAssignedCarId(driverDto.getAssignedCarId());
            
            Driver createdDriver = driverService.createDriver(driver);
            DriverDto createdDriverDto = new DriverDto(createdDriver);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Driver created successfully", createdDriverDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to create driver: " + e.getMessage()));
        }
    }

    // GET /api/drivers - Get all active drivers
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<DriverDto>>> getAllDrivers() {
        try {
            List<Driver> drivers = driverService.getAllActiveDrivers();
            List<DriverDto> driverDtos = drivers.stream()
                    .map(DriverDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Drivers retrieved successfully", driverDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve drivers: " + e.getMessage()));
        }
    }

    // GET /api/drivers/{id} - Get driver by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DriverDto>> getDriverById(@PathVariable Long id) {
        try {
            Driver driver = driverService.getDriverById(id);
            DriverDto driverDto = new DriverDto(driver);
            return ResponseEntity.ok(ApiResponseDto.success("Driver retrieved successfully", driverDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Driver not found: " + e.getMessage()));
        }
    }

    // GET /api/drivers/user/{userId} - Get driver by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<DriverDto>> getDriverByUserId(@PathVariable Long userId) {
        try {
            Driver driver = driverService.getDriverByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Driver not found for user ID: " + userId));
            DriverDto driverDto = new DriverDto(driver);
            return ResponseEntity.ok(ApiResponseDto.success("Driver retrieved successfully", driverDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Driver not found: " + e.getMessage()));
        }
    }

    // PUT /api/drivers/{id} - Update driver
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<DriverDto>> updateDriver(@PathVariable Long id, @Valid @RequestBody DriverDto driverDto) {
        try {
            Driver driver = new Driver();
            driver.setAssignedCarId(driverDto.getAssignedCarId());
            
            Driver updatedDriver = driverService.updateDriver(id, driver);
            DriverDto updatedDriverDto = new DriverDto(updatedDriver);
            return ResponseEntity.ok(ApiResponseDto.success("Driver updated successfully", updatedDriverDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to update driver: " + e.getMessage()));
        }
    }

    // DELETE /api/drivers/{id} - Soft delete driver (deactivate)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deactivateDriver(@PathVariable Long id) {
        try {
            driverService.deactivateDriver(id);
            return ResponseEntity.ok(ApiResponseDto.success("Driver deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to deactivate driver: " + e.getMessage()));
        }
    }

    // PUT /api/drivers/{id}/reactivate - Reactivate driver
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponseDto<String>> reactivateDriver(@PathVariable Long id) {
        try {
            driverService.reactivateDriver(id);
            return ResponseEntity.ok(ApiResponseDto.success("Driver reactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to reactivate driver: " + e.getMessage()));
        }
    }

    // PUT /api/drivers/{id}/assign-car - Assign car to driver
    @PutMapping("/{id}/assign-car")
    public ResponseEntity<ApiResponseDto<DriverDto>> assignCarToDriver(@PathVariable Long id, 
                                                                     @RequestParam Long carId) {
        try {
            Driver driver = driverService.assignCarToDriver(id, carId);
            DriverDto driverDto = new DriverDto(driver);
            return ResponseEntity.ok(ApiResponseDto.success("Car assigned to driver successfully", driverDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to assign car: " + e.getMessage()));
        }
    }

    // PUT /api/drivers/{id}/remove-car - Remove car from driver
    @PutMapping("/{id}/remove-car")
    public ResponseEntity<ApiResponseDto<DriverDto>> removeCarFromDriver(@PathVariable Long id) {
        try {
            Driver driver = driverService.unassignCarFromDriver(id);
            DriverDto driverDto = new DriverDto(driver);
            return ResponseEntity.ok(ApiResponseDto.success("Car removed from driver successfully", driverDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to remove car: " + e.getMessage()));
        }
    }

    // GET /api/drivers/available - Get available drivers (no car assigned)
    @GetMapping("/available")
    public ResponseEntity<ApiResponseDto<List<DriverDto>>> getAvailableDrivers() {
        try {
            List<Driver> drivers = driverService.getDriversWithoutCars();
            List<DriverDto> driverDtos = drivers.stream()
                    .map(DriverDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Available drivers retrieved successfully", driverDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve available drivers: " + e.getMessage()));
        }
    }

    // GET /api/drivers/assigned - Get assigned drivers (with car assigned)
    @GetMapping("/assigned")
    public ResponseEntity<ApiResponseDto<List<DriverDto>>> getAssignedDrivers() {
        try {
            List<Driver> drivers = driverService.getDriversWithCars();
            List<DriverDto> driverDtos = drivers.stream()
                    .map(DriverDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Assigned drivers retrieved successfully", driverDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve assigned drivers: " + e.getMessage()));
        }
    }

    // GET /api/drivers/stats/count - Get driver count statistics
    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponseDto<Object>> getDriverCountStats() {
        try {
            long totalDrivers = driverService.countActiveDrivers();
            long availableDrivers = driverService.countDriversWithoutCars();
            long assignedDrivers = driverService.countDriversWithCars();
            
            class DriverStats {
                public final long totalDrivers;
                public final long availableDrivers;
                public final long assignedDrivers;
                public DriverStats(long totalDrivers, long availableDrivers, long assignedDrivers) {
                    this.totalDrivers = totalDrivers;
                    this.availableDrivers = availableDrivers;
                    this.assignedDrivers = assignedDrivers;
                }
            }
            DriverStats stats = new DriverStats(totalDrivers, availableDrivers, assignedDrivers);
            return ResponseEntity.ok(ApiResponseDto.success("Driver statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve driver statistics: " + e.getMessage()));
        }
    }

    // GET /api/drivers/stats/detailed - Get detailed driver statistics
    @GetMapping("/stats/detailed")
    public ResponseEntity<ApiResponseDto<Object>> getDetailedDriverStats() {
        try {
            DriverService.DriverStatistics stats = driverService.getDriverStatistics();
            return ResponseEntity.ok(ApiResponseDto.success("Detailed driver statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve detailed driver statistics: " + e.getMessage()));
        }
    }
}
