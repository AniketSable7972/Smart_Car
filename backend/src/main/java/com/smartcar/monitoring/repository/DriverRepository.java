package com.smartcar.monitoring.repository;

import com.smartcar.monitoring.model.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    
    // Find by user ID
    Optional<Driver> findByUserId(Long userId);
    
    // Find by assigned car ID
    Optional<Driver> findByAssignedCarId(Long carId);
    
    // Find all active drivers
    List<Driver> findByIsActiveTrue();
    
    // Find drivers without assigned cars
    List<Driver> findByAssignedCarIdIsNullAndIsActiveTrue();
    
    // Find drivers with assigned cars
    List<Driver> findByAssignedCarIdIsNotNullAndIsActiveTrue();
    
    // Find drivers created in date range
    @Query("SELECT d FROM Driver d WHERE d.creationDate BETWEEN :startDate AND :endDate AND d.isActive = true")
    List<Driver> findDriversCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                          @Param("endDate") java.time.LocalDateTime endDate);
    
    // Count active drivers
    long countByIsActiveTrue();
    
    // Count drivers with assigned cars
    long countByAssignedCarIdIsNotNullAndIsActiveTrue();
    
    // Count drivers without assigned cars
    long countByAssignedCarIdIsNullAndIsActiveTrue();
}
