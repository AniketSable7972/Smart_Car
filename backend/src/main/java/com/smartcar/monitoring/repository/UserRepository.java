package com.smartcar.monitoring.repository;

import com.smartcar.monitoring.model.User;
import com.smartcar.monitoring.model.User.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find by username (for authentication)
    Optional<User> findByUsername(String username);
    
    // Find by email
    Optional<User> findByEmail(String email);
    
    // Find by role
    List<User> findByRole(UserRole role);
    
    // Find active users by role
    List<User> findByRoleAndIsActiveTrue(UserRole role);
    
    // Find all active users
    List<User> findByIsActiveTrue();
    
    // Find users by age range
    List<User> findByAgeBetweenAndIsActiveTrue(Integer minAge, Integer maxAge);
    
    // Find users by gender
    List<User> findByGenderAndIsActiveTrue(User.Gender gender);
    
    // Find users by contact number
    Optional<User> findByContactNumber(String contactNumber);
    
    // Check if username exists
    boolean existsByUsername(String username);
    
    // Check if email exists
    boolean existsByEmail(String email);
    
    // Check if contact number exists
    boolean existsByContactNumber(String contactNumber);
    
    // Custom query to find users created in date range
    @Query("SELECT u FROM User u WHERE u.creationDate BETWEEN :startDate AND :endDate AND u.isActive = true")
    List<User> findUsersCreatedBetween(@Param("startDate") java.time.LocalDateTime startDate, 
                                      @Param("endDate") java.time.LocalDateTime endDate);
    
    // Custom query to find users by name pattern
    @Query("SELECT u FROM User u WHERE u.name LIKE %:namePattern% AND u.isActive = true")
    List<User> findUsersByNamePattern(@Param("namePattern") String namePattern);
    
    // Count active users by role
    long countByRoleAndIsActiveTrue(UserRole role);
    
    // Count total active users
    long countByIsActiveTrue();
}
