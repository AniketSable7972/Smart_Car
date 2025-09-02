// CarService.java
package com.smartcar.monitoring.service;

import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.model.Driver;
import com.smartcar.monitoring.model.Telemetry;
import com.smartcar.monitoring.repository.CarRepository;
import com.smartcar.monitoring.repository.DriverRepository;
import com.smartcar.monitoring.repository.TelemetryRepository;
import com.smartcar.monitoring.exception.CarNotFoundException;
import com.smartcar.monitoring.exception.DriverNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CarService {

	@Autowired
	private CarRepository carRepository;

	@Autowired
	private DriverRepository driverRepository;

	@Autowired
	private TelemetryRepository telemetryRepository;

	@Autowired
	private AlertService alertService;

	// Create new car
	public Car createCar(Car car) {
		car.setCreationDate(LocalDateTime.now());
		car.setLastUpdateOn(LocalDateTime.now());
		car.setIsActive(true);

		// Set default values if not provided
		if (car.getSpeed() == null)
			car.setSpeed(0);
		if (car.getFuelLevel() == null)
			car.setFuelLevel(100);
		if (car.getTemperature() == null)
			car.setTemperature(25);
		if (car.getStatus() == null)
			car.setStatus("IDLE");

		return carRepository.save(car);
	}

	// Get car by ID
	public Car getCarById(Long id) {
		return carRepository.findById(id)
				.orElseThrow(() -> new CarNotFoundException("Car not found with ID: " + id));
	}

	// Get all active cars
	public List<Car> getAllActiveCars() {
		return carRepository.findByIsActiveTrue();
	}

	// Get cars by driver
	public List<Car> getCarsByDriver(Long driverId) {
		return carRepository.findByDriverIdAndIsActiveTrue(driverId);
	}

	// Get cars by status
	public List<Car> getCarsByStatus(String status) {
		return carRepository.findByStatusAndIsActiveTrue(status);
	}

	// Get cars with low fuel
	public List<Car> getCarsWithLowFuel(Integer fuelThreshold) {
		return carRepository.findByFuelLevelLessThanAndIsActiveTrue(fuelThreshold);
	}

	// Get cars by temperature range
	public List<Car> getCarsByTemperatureRange(Integer minTemp, Integer maxTemp) {
		return carRepository.findByTemperatureBetweenAndIsActiveTrue(minTemp, maxTemp);
	}

	// Get cars by speed range
	public List<Car> getCarsBySpeedRange(Integer minSpeed, Integer maxSpeed) {
		return carRepository.findBySpeedBetweenAndIsActiveTrue(minSpeed, maxSpeed);
	}

	// Get cars by location
	public List<Car> getCarsByLocation(String location) {
		return carRepository.findByLocationContainingAndIsActiveTrue(location);
	}

	// Get cars updated recently (for real-time monitoring)
	public List<Car> getCarsUpdatedSince(LocalDateTime since) {
		return carRepository.findCarsUpdatedSince(since);
	}

	// Update car status and sensor data
	public Car updateCarStatus(Long carId, String status, Integer speed, Integer fuelLevel,
			Integer temperature, String location) {
		Car car = getCarById(carId);

		car.setStatus(status);
		car.setSpeed(speed);
		car.setFuelLevel(fuelLevel);
		car.setTemperature(temperature);
		car.setLocation(location);
		car.setLastUpdateOn(LocalDateTime.now());

		// Create telemetry record
		Telemetry telemetry = new Telemetry();
		telemetry.setCar(car);
		telemetry.setTimestamp(LocalDateTime.now());
		telemetry.setSpeed(speed);
		telemetry.setFuel(fuelLevel);
		telemetry.setTemperature(temperature);
		telemetry.setLocation(location);
		telemetryRepository.save(telemetry);

		// Check for alerts based on thresholds
		checkAndCreateAlerts(car);

		return carRepository.save(car);
	}

	// Assign car to driver
	public Car assignCarToDriver(Long carId, Long driverId) {
		Car car = getCarById(carId);
		Driver driver = driverRepository.findById(driverId)
				.orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + driverId));

		car.setDriver(driver);
		car.setLastUpdateOn(LocalDateTime.now());

		// Update driver's assigned car
		driver.setAssignedCarId(carId);
		driverRepository.save(driver);

		return carRepository.save(car);
	}

	// Unassign car from driver
	public Car unassignCarFromDriver(Long carId) {
		Car car = getCarById(carId);

		if (car.getDriver() != null) {
			// Update driver's assigned car
			Driver driver = car.getDriver();
			driver.setAssignedCarId(null);
			driverRepository.save(driver);

			car.setDriver(null);
			car.setLastUpdateOn(LocalDateTime.now());
		}

		return carRepository.save(car);
	}

	// Update car
	public Car updateCar(Long id, Car carDetails) {
		Car car = getCarById(id);

		car.setStatus(carDetails.getStatus());
		car.setCarNumber(carDetails.getCarNumber());
		car.setCarModel(carDetails.getCarModel());
		car.setSpeed(carDetails.getSpeed());
		car.setFuelLevel(carDetails.getFuelLevel());
		car.setTemperature(carDetails.getTemperature());
		car.setLocation(carDetails.getLocation());
		car.setLastUpdateOn(LocalDateTime.now());

		return carRepository.save(car);
	}

	// Soft delete car
	public void deactivateCar(Long id) {
		Car car = getCarById(id);
		car.setIsActive(false);
		car.setLastUpdateOn(LocalDateTime.now());
		carRepository.save(car);
	}

	// Reactivate car
	public void reactivateCar(Long id) {
		Car car = getCarById(id);
		car.setIsActive(true);
		car.setLastUpdateOn(LocalDateTime.now());
		carRepository.save(car);
	}

	// Get cars without drivers
	public List<Car> getCarsWithoutDrivers() {
		return carRepository.findByDriverIsNullAndIsActiveTrue();
	}

	// Get cars with drivers
	public List<Car> getCarsWithDrivers() {
		return carRepository.findByDriverIsNotNullAndIsActiveTrue();
	}

	// Count active cars
	public long countActiveCars() {
		return carRepository.countByIsActiveTrue();
	}

	// Count cars by status
	public long countCarsByStatus(String status) {
		return carRepository.countByStatusAndIsActiveTrue(status);
	}

	// Count cars with low fuel
	public long countCarsWithLowFuel(Integer fuelThreshold) {
		return carRepository.countByFuelLevelLessThanAndIsActiveTrue(fuelThreshold);
	}

	// Count cars without drivers
	public long countCarsWithoutDrivers() {
		return carRepository.countByDriverIsNullAndIsActiveTrue();
	}

	// Count cars with drivers
	public long countCarsWithDrivers() {
		return carRepository.countByDriverIsNotNullAndIsActiveTrue();
	}

	// Check and create alerts based on thresholds
	private void checkAndCreateAlerts(Car car) {
		// Check fuel level
		if (car.getFuelLevel() < 20) {
			alertService.createAlert(car, "LOW_FUEL",
					car.getFuelLevel() < 10 ? "CRITICAL" : "HIGH",
					"Fuel level is critically low: " + car.getFuelLevel() + "%");
		}

		// Check temperature
		if (car.getTemperature() > 50) {
			alertService.createAlert(car, "HIGH_TEMPERATURE",
					car.getTemperature() > 60 ? "CRITICAL" : "HIGH",
					"Engine temperature is high: " + car.getTemperature() + "°C");
		}

		// Check speed
		if (car.getSpeed() > 120) {
			alertService.createAlert(car, "HIGH_SPEED",
					car.getSpeed() > 150 ? "CRITICAL" : "MEDIUM",
					"Vehicle speed is high: " + car.getSpeed() + " km/h");
		}
	}
}
