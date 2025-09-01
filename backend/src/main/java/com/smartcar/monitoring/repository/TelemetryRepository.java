package com.smartcar.monitoring.repository;

import com.smartcar.monitoring.model.Telemetry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TelemetryRepository extends JpaRepository<Telemetry, Long> {
	
	// Find by car ID
	List<Telemetry> findByCarIdAndIsActiveTrue(Long carId);
	
	// Find by trip ID
	List<Telemetry> findByTripIdAndIsActiveTrue(Long tripId);
	
	// Find by trip ID and time range
	List<Telemetry> findByTripIdAndTimestampBetweenAndIsActiveTrue(Long tripId, LocalDateTime startTime, LocalDateTime endTime);
	
	// Find by timestamp range
	List<Telemetry> findByTimestampBetweenAndIsActiveTrue(LocalDateTime startTime, LocalDateTime endTime);
	
	// Find by car ID and timestamp range
	List<Telemetry> findByCarIdAndTimestampBetweenAndIsActiveTrue(Long carId, LocalDateTime startTime, LocalDateTime endTime);
	
	// Find by fuel level threshold
	List<Telemetry> findByFuelLessThanAndIsActiveTrue(Integer fuelThreshold);
	
	// Find by temperature range
	List<Telemetry> findByTemperatureBetweenAndIsActiveTrue(Integer minTemp, Integer maxTemp);
	
	// Find by speed range
	List<Telemetry> findBySpeedBetweenAndIsActiveTrue(Integer minSpeed, Integer maxSpeed);
	
	// Find by location
	List<Telemetry> findByLocationContainingAndIsActiveTrue(String location);
	
	// Find all active telemetry records
	List<Telemetry> findByIsActiveTrue();
	
	// Find latest telemetry for each car
	@Query("SELECT t FROM Telemetry t WHERE t.id IN (SELECT MAX(t2.id) FROM Telemetry t2 WHERE t2.car.id = t.car.id AND t2.isActive = true GROUP BY t2.car.id) AND t.isActive = true")
	List<Telemetry> findLatestTelemetryForAllCars();
	
	// Find latest telemetry for specific car
	@Query("SELECT t FROM Telemetry t WHERE t.car.id = :carId AND t.isActive = true ORDER BY t.timestamp DESC")
	List<Telemetry> findLatestTelemetryByCarId(@Param("carId") Long carId);
	
	// Find telemetry records created in date range
	@Query("SELECT t FROM Telemetry t WHERE t.creationDate BETWEEN :startDate AND :endDate AND t.isActive = true")
	List<Telemetry> findTelemetryCreatedBetween(@Param("startDate") LocalDateTime startDate, 
												 @Param("endDate") LocalDateTime endDate);
	
	// Count telemetry records by car
	long countByCarIdAndIsActiveTrue(Long carId);
	
	// Count total active telemetry records
	long countByIsActiveTrue();
	
	// Count telemetry records in time range
	long countByTimestampBetweenAndIsActiveTrue(LocalDateTime startTime, LocalDateTime endTime);
	
	// Count telemetry records with low fuel
	long countByFuelLessThanAndIsActiveTrue(Integer fuelThreshold);
	
	// Count telemetry records with high temperature
	long countByTemperatureGreaterThanAndIsActiveTrue(Integer tempThreshold);
	
	// Count telemetry records with high speed
	long countBySpeedGreaterThanAndIsActiveTrue(Integer speedThreshold);
}
