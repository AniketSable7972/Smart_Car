package com.smartcar.monitoring.service;

import com.smartcar.monitoring.model.Driver;
import com.smartcar.monitoring.model.User;
import com.smartcar.monitoring.repository.DriverRepository;
import com.smartcar.monitoring.repository.UserRepository;
import com.smartcar.monitoring.exception.DriverNotFoundException;
import com.smartcar.monitoring.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DriverService {
    
    @Autowired
    private DriverRepository driverRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create new driver
    public Driver createDriver(Driver driver) {
        // Verify user exists and is a DRIVER
        User user = userRepository.findById(driver.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + driver.getUser().getId()));
        
        if (user.getRole() != User.UserRole.DRIVER) {
            throw new IllegalArgumentException("User must have DRIVER role");
        }
        
        driver.setCreationDate(LocalDateTime.now());
        driver.setLastUpdateOn(LocalDateTime.now());
        driver.setIsActive(true);
        
        return driverRepository.save(driver);
    }
    
    // Get driver by ID
    public Driver getDriverById(Long id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new DriverNotFoundException("Driver not found with ID: " + id));
    }
    
    // Get driver by user ID
    public Optional<Driver> getDriverByUserId(Long userId) {
        return driverRepository.findByUserId(userId);
    }
    
    // Get driver by assigned car ID
    public Optional<Driver> getDriverByAssignedCarId(Long carId) {
        return driverRepository.findByAssignedCarId(carId);
    }
    
    // Get all active drivers
    public List<Driver> getAllActiveDrivers() {
        return driverRepository.findByIsActiveTrue();
    }
    
    // Get drivers without assigned cars
    public List<Driver> getDriversWithoutCars() {
        return driverRepository.findByAssignedCarIdIsNullAndIsActiveTrue();
    }
    
    // Get drivers with assigned cars
    public List<Driver> getDriversWithCars() {
        return driverRepository.findByAssignedCarIdIsNotNullAndIsActiveTrue();
    }
    
    // Get drivers created in date range
    public List<Driver> getDriversCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return driverRepository.findDriversCreatedBetween(startDate, endDate);
    }
    
    // Update driver
    public Driver updateDriver(Long id, Driver driverDetails) {
        Driver driver = getDriverById(id);
        
        // Update assigned car if changed
        if (driverDetails.getAssignedCarId() != null && 
            !driverDetails.getAssignedCarId().equals(driver.getAssignedCarId())) {
            driver.setAssignedCarId(driverDetails.getAssignedCarId());
        }
        
        driver.setLastUpdateOn(LocalDateTime.now());
        
        return driverRepository.save(driver);
    }
    
    // Assign car to driver
    public Driver assignCarToDriver(Long driverId, Long carId) {
        Driver driver = getDriverById(driverId);
        driver.setAssignedCarId(carId);
        driver.setLastUpdateOn(LocalDateTime.now());
        return driverRepository.save(driver);
    }
    
    // Unassign car from driver
    public Driver unassignCarFromDriver(Long driverId) {
        Driver driver = getDriverById(driverId);
        driver.setAssignedCarId(null);
        driver.setLastUpdateOn(LocalDateTime.now());
        return driverRepository.save(driver);
    }
    
    // Soft delete driver
    public void deactivateDriver(Long id) {
        Driver driver = getDriverById(id);
        driver.setIsActive(false);
        driver.setLastUpdateOn(LocalDateTime.now());
        driverRepository.save(driver);
    }
    
    // Reactivate driver
    public void reactivateDriver(Long id) {
        Driver driver = getDriverById(id);
        driver.setIsActive(true);
        driver.setLastUpdateOn(LocalDateTime.now());
        driverRepository.save(driver);
    }
    
    // Count active drivers
    public long countActiveDrivers() {
        return driverRepository.countByIsActiveTrue();
    }
    
    // Count drivers with assigned cars
    public long countDriversWithCars() {
        return driverRepository.countByAssignedCarIdIsNotNullAndIsActiveTrue();
    }
    
    // Count drivers without assigned cars
    public long countDriversWithoutCars() {
        return driverRepository.countByAssignedCarIdIsNullAndIsActiveTrue();
    }
    
    // Get driver statistics
    public DriverStatistics getDriverStatistics() {
        DriverStatistics stats = new DriverStatistics();
        
        stats.setTotalDrivers(countActiveDrivers());
        stats.setDriversWithCars(countDriversWithCars());
        stats.setDriversWithoutCars(countDriversWithoutCars());
        
        return stats;
    }
    
    // Inner class for driver statistics
    public static class DriverStatistics {
        private long totalDrivers;
        private long driversWithCars;
        private long driversWithoutCars;
        
        // Getters and setters
        public long getTotalDrivers() { return totalDrivers; }
        public void setTotalDrivers(long totalDrivers) { this.totalDrivers = totalDrivers; }
        
        public long getDriversWithCars() { return driversWithCars; }
        public void setDriversWithCars(long driversWithCars) { this.driversWithCars = driversWithCars; }
        
        public long getDriversWithoutCars() { return driversWithoutCars; }
        public void setDriversWithoutCars(long driversWithoutCars) { this.driversWithoutCars = driversWithoutCars; }
    }
}
