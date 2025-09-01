package com.smartcar.monitoring.repository;

import com.smartcar.monitoring.model.Trip;
import com.smartcar.monitoring.model.Trip.TripStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {
    List<Trip> findByDriverIdAndIsActiveTrue(Long driverId);
    List<Trip> findByStatus(TripStatus status);
    List<Trip> findByStatusIn(List<TripStatus> statuses);
    Optional<Trip> findFirstByDriverIdAndStatusInOrderByCreationDateDesc(Long driverId, List<TripStatus> statuses);
    Optional<Trip> findFirstByDriverIdAndStatus(Long driverId, TripStatus status);
    Optional<Trip> findFirstByCarIdAndStatus(Long carId, TripStatus status);
    List<Trip> findByCarIdAndStatus(Long carId, TripStatus status);
}