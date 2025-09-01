package com.smartcar.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;
    
    @Column(nullable = false)
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Role is required")
    private UserRole role;
    
    @Column(nullable = false)
    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;
    
    @Column(nullable = false)
    @Min(value = 18, message = "Age must be at least 18")
    @Max(value = 100, message = "Age cannot exceed 100")
    private Integer age;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @NotNull(message = "Gender is required")
    private Gender gender;
    
    @Column(name = "contact_number", nullable = false)
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Invalid contact number format")
    private String contactNumber;
    
    @Column(unique = true, nullable = false)
    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is required")
    private String email;

    @Column(name = "license_number", nullable = false)
    @NotBlank(message = "License number is required")
    private String licenseNumber;
    
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
    
    @Column(name = "last_update_on")
    private LocalDateTime lastUpdateOn;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Enums
    public enum UserRole {
        ADMIN, DRIVER
    }
    
    public enum Gender {
        MALE, FEMALE, OTHER
    }
    
    // Constructors
    public User() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdateOn = LocalDateTime.now();
        this.isActive = true;
    }
    
    public User(String username, String password, UserRole role, String name, Integer age, 
                Gender gender, String contactNumber, String email, String licenseNumber) {
        this();
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
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getAge() {
        return age;
    }
    
    public void setAge(Integer age) {
        this.age = age;
    }
    
    public Gender getGender() {
        return gender;
    }
    
    public void setGender(Gender gender) {
        this.gender = gender;
    }
    
    public String getContactNumber() {
        return contactNumber;
    }
    
    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(String licenseNumber) {
        this.licenseNumber = licenseNumber;
    }
    
    public LocalDateTime getCreationDate() {
        return creationDate;
    }
    
    public void setCreationDate(LocalDateTime creationDate) {
        this.creationDate = creationDate;
    }
    
    public LocalDateTime getLastUpdateOn() {
        return lastUpdateOn;
    }
    
    public void setLastUpdateOn(LocalDateTime lastUpdateOn) {
        this.lastUpdateOn = lastUpdateOn;
    }
    
    public Boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
    
    // Pre-update hook
    @PreUpdate
    public void preUpdate() {
        this.lastUpdateOn = LocalDateTime.now();
    }
    
    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
