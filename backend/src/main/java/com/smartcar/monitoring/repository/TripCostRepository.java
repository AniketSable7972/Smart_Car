// TripCostRepository.java
package com.smartcar.monitoring.repository;

import com.smartcar.monitoring.model.TripCost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TripCostRepository extends JpaRepository<TripCost, Long> {
    Optional<TripCost> findByStartPointAndEndPointAndIsActiveTrue(String startPoint, String endPoint);

    List<TripCost> findByIsActiveTrue();
}