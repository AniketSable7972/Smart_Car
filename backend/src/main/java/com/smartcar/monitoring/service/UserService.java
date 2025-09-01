package com.smartcar.monitoring.service;

import com.smartcar.monitoring.model.User;
import com.smartcar.monitoring.model.User.UserRole;
import com.smartcar.monitoring.repository.UserRepository;
import com.smartcar.monitoring.exception.UserNotFoundException;
import com.smartcar.monitoring.exception.UserAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    // Create new user
    public User createUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + user.getUsername());
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + user.getEmail());
        }
        
        // Check if contact number already exists
        if (userRepository.existsByContactNumber(user.getContactNumber())) {
            throw new UserAlreadyExistsException("Contact number already exists: " + user.getContactNumber());
        }
        
        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        
        // Set creation date and active status
        user.setCreationDate(LocalDateTime.now());
        user.setLastUpdateOn(LocalDateTime.now());
        user.setIsActive(true);
        
        return userRepository.save(user);
    }
    
    // Get user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }
    
    // Get user by username (for authentication)
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    // Get user by email
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    // Get all active users
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    // Get users by role
    public List<User> getUsersByRole(UserRole role) {
        return userRepository.findByRoleAndIsActiveTrue(role);
    }
    
    // Update user
    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        
        // Check if new username conflicts (if changed)
        if (!user.getUsername().equals(userDetails.getUsername()) && 
            userRepository.existsByUsername(userDetails.getUsername())) {
            throw new UserAlreadyExistsException("Username already exists: " + userDetails.getUsername());
        }
        
        // Check if new email conflicts (if changed)
        if (!user.getEmail().equals(userDetails.getEmail()) && 
            userRepository.existsByEmail(userDetails.getEmail())) {
            throw new UserAlreadyExistsException("Email already exists: " + userDetails.getEmail());
        }
        
        // Check if new contact number conflicts (if changed)
        if (!user.getContactNumber().equals(userDetails.getContactNumber()) && 
            userRepository.existsByContactNumber(userDetails.getContactNumber())) {
            throw new UserAlreadyExistsException("Contact number already exists: " + userDetails.getContactNumber());
        }
        
        // Update fields
        user.setName(userDetails.getName());
        user.setAge(userDetails.getAge());
        user.setGender(userDetails.getGender());
        user.setContactNumber(userDetails.getContactNumber());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        user.setLastUpdateOn(LocalDateTime.now());
        
        // Update password only if provided
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }
        
        return userRepository.save(user);
    }
    
    // Soft delete user (mark as inactive)
    public void deactivateUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(false);
        user.setLastUpdateOn(LocalDateTime.now());
        userRepository.save(user);
    }
    
    // Reactivate user
    public void reactivateUser(Long id) {
        User user = getUserById(id);
        user.setIsActive(true);
        user.setLastUpdateOn(LocalDateTime.now());
        userRepository.save(user);
    }
    
    // Get users by age range
    public List<User> getUsersByAgeRange(Integer minAge, Integer maxAge) {
        return userRepository.findByAgeBetweenAndIsActiveTrue(minAge, maxAge);
    }
    
    // Get users by gender
    public List<User> getUsersByGender(User.Gender gender) {
        return userRepository.findByGenderAndIsActiveTrue(gender);
    }
    
    // Get users created in date range
    public List<User> getUsersCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.findUsersCreatedBetween(startDate, endDate);
    }
    
    // Get users by name pattern
    public List<User> getUsersByNamePattern(String namePattern) {
        return userRepository.findUsersByNamePattern(namePattern);
    }
    
    // Count active users by role
    public long countActiveUsersByRole(UserRole role) {
        return userRepository.countByRoleAndIsActiveTrue(role);
    }
    
    // Count total active users
    public long countTotalActiveUsers() {
        return userRepository.countByIsActiveTrue();
    }
    
    // Validate user credentials
    public boolean validateCredentials(String username, String rawPassword) {
        Optional<User> userOpt = getUserByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            return user.getIsActive() && passwordEncoder.matches(rawPassword, user.getPassword());
        }
        return false;
    }
}
