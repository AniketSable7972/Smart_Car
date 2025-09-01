package com.smartcar.monitoring.service;

import com.smartcar.monitoring.model.Telemetry;
import com.smartcar.monitoring.model.Car;
import com.smartcar.monitoring.repository.TelemetryRepository;
import com.smartcar.monitoring.repository.CarRepository;
import com.smartcar.monitoring.exception.TelemetryNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TelemetryService {
    
    @Autowired
    private TelemetryRepository telemetryRepository;
    
    @Autowired
    private CarRepository carRepository;
    
    // Create new telemetry record
    public Telemetry createTelemetry(Telemetry telemetry) {
        telemetry.setCreationDate(LocalDateTime.now());
        telemetry.setLastUpdateOn(LocalDateTime.now());
        telemetry.setIsActive(true);
        
        if (telemetry.getTimestamp() == null) {
            telemetry.setTimestamp(LocalDateTime.now());
        }
        
        return telemetryRepository.save(telemetry);
    }
    
    // Get telemetry by ID
    public Telemetry getTelemetryById(Long id) {
        return telemetryRepository.findById(id)
                .orElseThrow(() -> new TelemetryNotFoundException("Telemetry not found with ID: " + id));
    }
    
    // Get all active telemetry records
    public List<Telemetry> getAllActiveTelemetry() {
        return telemetryRepository.findByIsActiveTrue();
    }
    
    // Get telemetry by car
    public List<Telemetry> getTelemetryByCar(Long carId) {
        return telemetryRepository.findByCarIdAndIsActiveTrue(carId);
    }
    
    // Get telemetry by trip
    public List<Telemetry> getTelemetryByTrip(Long tripId) {
        return telemetryRepository.findByTripIdAndIsActiveTrue(tripId);
    }
    
    // Get telemetry by timestamp range
    public List<Telemetry> getTelemetryByTimestampRange(LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryRepository.findByTimestampBetweenAndIsActiveTrue(startTime, endTime);
    }
    
    // Get telemetry by trip and timestamp range
    public List<Telemetry> getTelemetryByTripAndTimestampRange(Long tripId, LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryRepository.findByTripIdAndTimestampBetweenAndIsActiveTrue(tripId, startTime, endTime);
    }
    
    // Get telemetry by car and timestamp range
    public List<Telemetry> getTelemetryByCarAndTimestampRange(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryRepository.findByCarIdAndTimestampBetweenAndIsActiveTrue(carId, startTime, endTime);
    }
    
    // Get telemetry by fuel level threshold
    public List<Telemetry> getTelemetryByFuelThreshold(Integer fuelThreshold) {
        return telemetryRepository.findByFuelLessThanAndIsActiveTrue(fuelThreshold);
    }
    
    // Get telemetry by temperature range
    public List<Telemetry> getTelemetryByTemperatureRange(Integer minTemp, Integer maxTemp) {
        return telemetryRepository.findByTemperatureBetweenAndIsActiveTrue(minTemp, maxTemp);
    }
    
    // Get telemetry by speed range
    public List<Telemetry> getTelemetryBySpeedRange(Integer minSpeed, Integer maxSpeed) {
        return telemetryRepository.findBySpeedBetweenAndIsActiveTrue(minSpeed, maxSpeed);
    }
    
    // Get telemetry by location
    public List<Telemetry> getTelemetryByLocation(String location) {
        return telemetryRepository.findByLocationContainingAndIsActiveTrue(location);
    }
    
    // Get latest telemetry for all cars
    public List<Telemetry> getLatestTelemetryForAllCars() {
        return telemetryRepository.findLatestTelemetryForAllCars();
    }
    
    // Get latest telemetry for specific car
    public List<Telemetry> getLatestTelemetryByCar(Long carId) {
        return telemetryRepository.findLatestTelemetryByCarId(carId);
    }
    
    // Get telemetry records created in date range
    public List<Telemetry> getTelemetryCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return telemetryRepository.findTelemetryCreatedBetween(startDate, endDate);
    }
    
    // Update telemetry
    public Telemetry updateTelemetry(Long id, Telemetry telemetryDetails) {
        Telemetry telemetry = getTelemetryById(id);
        
        telemetry.setSpeed(telemetryDetails.getSpeed());
        telemetry.setFuel(telemetryDetails.getFuel());
        telemetry.setTemperature(telemetryDetails.getTemperature());
        telemetry.setLocation(telemetryDetails.getLocation());
        telemetry.setLastUpdateOn(LocalDateTime.now());
        
        return telemetryRepository.save(telemetry);
    }
    
    // Soft delete telemetry
    public void deactivateTelemetry(Long id) {
        Telemetry telemetry = getTelemetryById(id);
        telemetry.setIsActive(false);
        telemetry.setLastUpdateOn(LocalDateTime.now());
        telemetryRepository.save(telemetry);
    }
    
    // Reactivate telemetry
    public void reactivateTelemetry(Long id) {
        Telemetry telemetry = getTelemetryById(id);
        telemetry.setIsActive(true);
        telemetry.setLastUpdateOn(LocalDateTime.now());
        telemetryRepository.save(telemetry);
    }
    
    // Count telemetry records by car
    public long countTelemetryByCar(Long carId) {
        return telemetryRepository.countByCarIdAndIsActiveTrue(carId);
    }
    
    // Count total active telemetry records
    public long countTotalActiveTelemetry() {
        return telemetryRepository.countByIsActiveTrue();
    }
    
    // Count telemetry records in time range
    public long countTelemetryInTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        return telemetryRepository.countByTimestampBetweenAndIsActiveTrue(startTime, endTime);
    }
    
    // Count telemetry records with low fuel
    public long countTelemetryWithLowFuel(Integer fuelThreshold) {
        return telemetryRepository.countByFuelLessThanAndIsActiveTrue(fuelThreshold);
    }
    
    // Count telemetry records with high temperature
    public long countTelemetryWithHighTemperature(Integer tempThreshold) {
        return telemetryRepository.countByTemperatureGreaterThanAndIsActiveTrue(tempThreshold);
    }
    
    // Count telemetry records with high speed
    public long countTelemetryWithHighSpeed(Integer speedThreshold) {
        return telemetryRepository.countBySpeedGreaterThanAndIsActiveTrue(speedThreshold);
    }
    
    // Get telemetry statistics for dashboard
    public TelemetryStatistics getTelemetryStatistics(Long carId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Telemetry> telemetryList = getTelemetryByCarAndTimestampRange(carId, startTime, endTime);
        
        if (telemetryList.isEmpty()) {
            return new TelemetryStatistics();
        }
        
        TelemetryStatistics stats = new TelemetryStatistics();
        stats.setTotalRecords(telemetryList.size());
        
        // Calculate averages
        double avgSpeed = telemetryList.stream().mapToInt(Telemetry::getSpeed).average().orElse(0.0);
        double avgFuel = telemetryList.stream().mapToInt(Telemetry::getFuel).average().orElse(0.0);
        double avgTemperature = telemetryList.stream().mapToInt(Telemetry::getTemperature).average().orElse(0.0);
        
        stats.setAverageSpeed(Math.round(avgSpeed * 100.0) / 100.0);
        stats.setAverageFuel(Math.round(avgFuel * 100.0) / 100.0);
        stats.setAverageTemperature(Math.round(avgTemperature * 100.0) / 100.0);
        
        // Find min/max values
        stats.setMinSpeed(telemetryList.stream().mapToInt(Telemetry::getSpeed).min().orElse(0));
        stats.setMaxSpeed(telemetryList.stream().mapToInt(Telemetry::getSpeed).max().orElse(0));
        stats.setMinFuel(telemetryList.stream().mapToInt(Telemetry::getFuel).min().orElse(0));
        stats.setMaxFuel(telemetryList.stream().mapToInt(Telemetry::getFuel).max().orElse(0));
        stats.setMinTemperature(telemetryList.stream().mapToInt(Telemetry::getTemperature).min().orElse(0));
        stats.setMaxTemperature(telemetryList.stream().mapToInt(Telemetry::getTemperature).max().orElse(0));
        
        return stats;
    }
    
    // Get telemetry statistics for a specific trip
    public TelemetryStatistics getTelemetryStatisticsByTrip(Long tripId) {
        List<Telemetry> telemetryList = getTelemetryByTrip(tripId);
        if (telemetryList.isEmpty()) {
            return new TelemetryStatistics();
        }
        TelemetryStatistics stats = new TelemetryStatistics();
        stats.setTotalRecords(telemetryList.size());
        double avgSpeed = telemetryList.stream().mapToInt(Telemetry::getSpeed).average().orElse(0.0);
        double avgFuel = telemetryList.stream().mapToInt(Telemetry::getFuel).average().orElse(0.0);
        double avgTemperature = telemetryList.stream().mapToInt(Telemetry::getTemperature).average().orElse(0.0);
        stats.setAverageSpeed(Math.round(avgSpeed * 100.0) / 100.0);
        stats.setAverageFuel(Math.round(avgFuel * 100.0) / 100.0);
        stats.setAverageTemperature(Math.round(avgTemperature * 100.0) / 100.0);
        stats.setMinSpeed(telemetryList.stream().mapToInt(Telemetry::getSpeed).min().orElse(0));
        stats.setMaxSpeed(telemetryList.stream().mapToInt(Telemetry::getSpeed).max().orElse(0));
        stats.setMinFuel(telemetryList.stream().mapToInt(Telemetry::getFuel).min().orElse(0));
        stats.setMaxFuel(telemetryList.stream().mapToInt(Telemetry::getFuel).max().orElse(0));
        stats.setMinTemperature(telemetryList.stream().mapToInt(Telemetry::getTemperature).min().orElse(0));
        stats.setMaxTemperature(telemetryList.stream().mapToInt(Telemetry::getTemperature).max().orElse(0));
        return stats;
    }
    
    // Inner class for telemetry statistics
    public static class TelemetryStatistics {
        private int totalRecords;
        private double averageSpeed;
        private double averageFuel;
        private double averageTemperature;
        private int minSpeed;
        private int maxSpeed;
        private int minFuel;
        private int maxFuel;
        private int minTemperature;
        private int maxTemperature;
        
        // Getters and setters
        public int getTotalRecords() { return totalRecords; }
        public void setTotalRecords(int totalRecords) { this.totalRecords = totalRecords; }
        
        public double getAverageSpeed() { return averageSpeed; }
        public void setAverageSpeed(double averageSpeed) { this.averageSpeed = averageSpeed; }
        
        public double getAverageFuel() { return averageFuel; }
        public void setAverageFuel(double averageFuel) { this.averageFuel = averageFuel; }
        
        public double getAverageTemperature() { return averageTemperature; }
        public void setAverageTemperature(double averageTemperature) { this.averageTemperature = averageTemperature; }
        
        public int getMinSpeed() { return minSpeed; }
        public void setMinSpeed(int minSpeed) { this.minSpeed = minSpeed; }
        
        public int getMaxSpeed() { return maxSpeed; }
        public void setMaxSpeed(int maxSpeed) { this.maxSpeed = maxSpeed; }
        
        public int getMinFuel() { return minFuel; }
        public void setMinFuel(int minFuel) { this.minFuel = minFuel; }
        
        public int getMaxFuel() { return maxFuel; }
        public void setMaxFuel(int maxFuel) { this.maxFuel = maxFuel; }
        
        public int getMinTemperature() { return minTemperature; }
        public void setMinTemperature(int minTemperature) { this.minTemperature = minTemperature; }
        
        public int getMaxTemperature() { return maxTemperature; }
        public void setMaxTemperature(int maxTemperature) { this.maxTemperature = maxTemperature; }
    }
}
