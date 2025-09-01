package com.smartcar.monitoring.repository;

import com.smartcar.monitoring.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {
    
    // Find by user ID
    Optional<Admin> findByUserId(Long userId);
    
    // Find all active admins
    List<Admin> findByIsActiveTrue();
    
    // Find admins by permissions
    List<Admin> findByPermissionsContainingAndIsActiveTrue(String permission);
    
    // Find admins created in date range
    @Query("SELECT a FROM Admin a WHERE a.creationDate BETWEEN :startDate AND :endDate AND a.isActive = true")
    List<Admin> findAdminsCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                        @Param("endDate") java.time.LocalDateTime endDate);
    
    // Count active admins
    long countByIsActiveTrue();
    
    // Count admins with specific permission
    long countByPermissionsContainingAndIsActiveTrue(String permission);
}
