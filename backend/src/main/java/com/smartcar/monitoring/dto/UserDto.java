package com.smartcar.monitoring.dto;

import com.smartcar.monitoring.model.User;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class UserDto {
    private Long id;
    
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @NotNull(message = "Role is required")
    private User.UserRole role;
    
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age cannot exceed 100")
    private Integer age;
    
    @NotNull(message = "Gender is required")
    private User.Gender gender;
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid contact number format")
    private String contactNumber;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "License number is required")
    private String licenseNumber;
    
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateOn;
    private Boolean isActive;
    
    // Default constructor
    public UserDto() {}
    
    // Constructor from User entity
    public UserDto(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.role = user.getRole();
        this.name = user.getName();
        this.age = user.getAge();
        this.gender = user.getGender();
        this.contactNumber = user.getContactNumber();
        this.email = user.getEmail();
        this.licenseNumber = user.getLicenseNumber();
        this.creationDate = user.getCreationDate();
        this.lastUpdateOn = user.getLastUpdateOn();
        this.isActive = user.getIsActive();
    }
    
    // Constructor for creation (without ID and timestamps)
    public UserDto(String username, String password, User.UserRole role, String name, 
                   Integer age, User.Gender gender, String contactNumber, String email, String licenseNumber) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.name = name;
        this.age = age;
        this.gender = gender;
        this.contactNumber = contactNumber;
        this.email = email;
        this.licenseNumber = licenseNumber;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public User.UserRole getRole() { return role; }
    public void setRole(User.UserRole role) { this.role = role; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    
    public User.Gender getGender() { return gender; }
    public void setGender(User.Gender gender) { this.gender = gender; }
    
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
    
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    
    public LocalDateTime getLastUpdateOn() { return lastUpdateOn; }
    public void setLastUpdateOn(LocalDateTime lastUpdateOn) { this.lastUpdateOn = lastUpdateOn; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    // Convert to User entity
    public User toEntity() {
        User user = new User();
        user.setUsername(this.username);
        user.setPassword(this.password);
        user.setRole(this.role);
        user.setName(this.name);
        user.setAge(this.age);
        user.setGender(this.gender);
        user.setContactNumber(this.contactNumber);
        user.setEmail(this.email);
        user.setLicenseNumber(this.licenseNumber);
        return user;
    }
}
