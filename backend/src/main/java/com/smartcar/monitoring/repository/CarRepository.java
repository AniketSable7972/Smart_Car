package com.smartcar.monitoring.repository;

import com.smartcar.monitoring.model.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
    
    // Find by driver ID
    List<Car> findByDriverIdAndIsActiveTrue(Long driverId);
    
    // Find by status
    List<Car> findByStatusAndIsActiveTrue(String status);
    
    // Find cars by fuel level threshold
    List<Car> findByFuelLevelLessThanAndIsActiveTrue(Integer fuelThreshold);
    
    // Find cars by temperature range
    List<Car> findByTemperatureBetweenAndIsActiveTrue(Integer minTemp, Integer maxTemp);
    
    // Find cars by speed range
    List<Car> findBySpeedBetweenAndIsActiveTrue(Integer minSpeed, Integer maxSpeed);
    
    // Find cars by location
    List<Car> findByLocationContainingAndIsActiveTrue(String location);
    
    // Find all active cars
    List<Car> findByIsActiveTrue();
    
    // Find cars without assigned drivers
    List<Car> findByDriverIsNullAndIsActiveTrue();
    
    // Find cars with assigned drivers
    List<Car> findByDriverIsNotNullAndIsActiveTrue();
    
    // Find cars updated in time range (for real-time monitoring)
    @Query("SELECT c FROM Car c WHERE c.lastUpdateOn >= :since AND c.isActive = true")
    List<Car> findCarsUpdatedSince(@Param("since") java.time.LocalDateTime since);
    
    // Find cars by creation date range
    @Query("SELECT c FROM Car c WHERE c.creationDate BETWEEN :startDate AND :endDate AND c.isActive = true")
    List<Car> findCarsCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                    @Param("endDate") java.time.LocalDateTime endDate);
    
    // Count active cars
    long countByIsActiveTrue();
    
    // Count cars by status
    long countByStatusAndIsActiveTrue(String status);
    
    // Count cars with low fuel
    long countByFuelLevelLessThanAndIsActiveTrue(Integer fuelThreshold);
    
    // Count cars without drivers
    long countByDriverIsNullAndIsActiveTrue();
    
    // Count cars with drivers
    long countByDriverIsNotNullAndIsActiveTrue();
}
