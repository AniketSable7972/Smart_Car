package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.*;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.service.CarService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cars")
@CrossOrigin(origins = "*")
public class CarController {

    @Autowired
    private CarService carService;

    // POST /api/cars - Create new car
    @PostMapping
    public ResponseEntity<ApiResponseDto<CarDto>> createCar(@Valid @RequestBody CarDto carDto) {
        try {
            Car car = new Car(carDto.getStatus(), carDto.getSpeed(), carDto.getFuelLevel(), 
                             carDto.getTemperature(), carDto.getLocation());
            car.setCarNumber(carDto.getCarNumber());
            car.setCarModel(carDto.getCarModel());
            Car createdCar = carService.createCar(car);
            CarDto createdCarDto = new CarDto(createdCar);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Car created successfully", createdCarDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to create car: " + e.getMessage()));
        }
    }

    // GET /api/cars - Get all active cars
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<CarDto>>> getAllCars() {
        try {
            List<Car> cars = carService.getAllActiveCars();
            List<CarDto> carDtos = cars.stream()
                    .map(CarDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Cars retrieved successfully", carDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve cars: " + e.getMessage()));
        }
    }

    // GET /api/cars/{id} - Get car by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CarDto>> getCarById(@PathVariable Long id) {
        try {
            Car car = carService.getCarById(id);
            CarDto carDto = new CarDto(car);
            return ResponseEntity.ok(ApiResponseDto.success("Car retrieved successfully", carDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Car not found: " + e.getMessage()));
        }
    }

    // PUT /api/cars/{id} - Update car
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<CarDto>> updateCar(@PathVariable Long id, @Valid @RequestBody CarDto carDto) {
        try {
            Car car = new Car(carDto.getStatus(), carDto.getSpeed(), carDto.getFuelLevel(), 
                             carDto.getTemperature(), carDto.getLocation());
            car.setId(id);
            car.setCarNumber(carDto.getCarNumber());
            car.setCarModel(carDto.getCarModel());
            Car updatedCar = carService.updateCar(id, car);
            CarDto updatedCarDto = new CarDto(updatedCar);
            return ResponseEntity.ok(ApiResponseDto.success("Car updated successfully", updatedCarDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to update car: " + e.getMessage()));
        }
    }

    // DELETE /api/cars/{id} - Soft delete car (deactivate)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deactivateCar(@PathVariable Long id) {
        try {
            carService.deactivateCar(id);
            return ResponseEntity.ok(ApiResponseDto.success("Car deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to deactivate car: " + e.getMessage()));
        }
    }

    // PUT /api/cars/{id}/reactivate - Reactivate car
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponseDto<String>> reactivateCar(@PathVariable Long id) {
        try {
            carService.reactivateCar(id);
            return ResponseEntity.ok(ApiResponseDto.success("Car reactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to reactivate car: " + e.getMessage()));
        }
    }

    // PUT /api/cars/{id}/assign-driver - Assign driver to car
    @PutMapping("/{id}/assign-driver")
    public ResponseEntity<ApiResponseDto<CarDto>> assignDriverToCar(@PathVariable Long id, 
                                                                   @RequestParam Long driverId) {
        try {
            Car car = carService.assignCarToDriver(id, driverId);
            CarDto carDto = new CarDto(car);
            return ResponseEntity.ok(ApiResponseDto.success("Driver assigned to car successfully", carDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to assign driver: " + e.getMessage()));
        }
    }

    // PUT /api/cars/{id}/remove-driver - Remove driver from car
    @PutMapping("/{id}/remove-driver")
    public ResponseEntity<ApiResponseDto<CarDto>> removeDriverFromCar(@PathVariable Long id) {
        try {
            Car car = carService.unassignCarFromDriver(id);
            CarDto carDto = new CarDto(car);
            return ResponseEntity.ok(ApiResponseDto.success("Driver removed from car successfully", carDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to remove driver: " + e.getMessage()));
        }
    }

    // GET /api/cars/status/{status} - Get cars by status
    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponseDto<List<CarDto>>> getCarsByStatus(@PathVariable String status) {
        try {
            List<Car> cars = carService.getCarsByStatus(status);
            List<CarDto> carDtos = cars.stream()
                    .map(CarDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Cars retrieved successfully", carDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve cars: " + e.getMessage()));
        }
    }

    // GET /api/cars/driver/{driverId} - Get cars by driver
    @GetMapping("/driver/{driverId}")
    public ResponseEntity<ApiResponseDto<List<CarDto>>> getCarsByDriver(@PathVariable Long driverId) {
        try {
            List<Car> cars = carService.getCarsByDriver(driverId);
            List<CarDto> carDtos = cars.stream()
                    .map(CarDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Cars retrieved successfully", carDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve cars: " + e.getMessage()));
        }
    }

    // GET /api/cars/available - Get available cars (no driver assigned)
    @GetMapping("/available")
    public ResponseEntity<ApiResponseDto<List<CarDto>>> getAvailableCars() {
        try {
            List<Car> cars = carService.getCarsWithoutDrivers();
            List<CarDto> carDtos = cars.stream()
                    .map(CarDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Available cars retrieved successfully", carDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve available cars: " + e.getMessage()));
        }
    }

    // GET /api/cars/stats/count - Get car count statistics
    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponseDto<Object>> getCarCountStats() {
        try {
            long totalCars = carService.countActiveCars();
            long availableCars = carService.countCarsWithoutDrivers();
            long assignedCars = carService.countCarsWithDrivers();
            
            class CarStats {
                public final long totalCars;
                public final long availableCars;
                public final long assignedCars;
                public CarStats(long totalCars, long availableCars, long assignedCars) {
                    this.totalCars = totalCars;
                    this.availableCars = availableCars;
                    this.assignedCars = assignedCars;
                }
            }
            CarStats stats = new CarStats(totalCars, availableCars, assignedCars);
            return ResponseEntity.ok(ApiResponseDto.success("Car statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve car statistics: " + e.getMessage()));
        }
    }

    // POST /api/cars/{id}/update-telemetry - Update car telemetry data
    @PostMapping("/{id}/update-telemetry")
    public ResponseEntity<ApiResponseDto<CarDto>> updateCarTelemetry(@PathVariable Long id, 
                                                                    @Valid @RequestBody CarDto carDto) {
        try {
            Car updatedCar = carService.updateCarStatus(id, carDto.getStatus(), carDto.getSpeed(), 
                                                       carDto.getFuelLevel(), carDto.getTemperature(), 
                                                       carDto.getLocation());
            CarDto updatedCarDto = new CarDto(updatedCar);
            return ResponseEntity.ok(ApiResponseDto.success("Car telemetry updated successfully", updatedCarDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to update car telemetry: " + e.getMessage()));
        }
    }
}
