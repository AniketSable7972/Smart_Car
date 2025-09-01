package com.smartcar.monitoring.dto;

import com.smartcar.monitoring.model.Admin;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class AdminDto {
    private Long id;
    
    @NotNull(message = "User ID is required")
    private Long userId;
    
    private String username;
    private String name;
    private String email;
    
    @NotBlank(message = "Permissions are required")
    @Size(max = 500, message = "Permissions cannot exceed 500 characters")
    private String permissions;
    
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdateOn;
    private Boolean isActive;
    
    // Default constructor
    public AdminDto() {}
    
    // Constructor from Admin entity
    public AdminDto(Admin admin) {
        this.id = admin.getId();
        this.userId = admin.getUser().getId();
        this.permissions = admin.getPermissions();
        this.creationDate = admin.getCreationDate();
        this.lastUpdateOn = admin.getLastUpdateOn();
        this.isActive = admin.getIsActive();
        
        if (admin.getUser() != null) {
            this.username = admin.getUser().getUsername();
            this.name = admin.getUser().getName();
            this.email = admin.getUser().getEmail();
        }
    }
    
    // Constructor for creation
    public AdminDto(Long userId, String permissions) {
        this.userId = userId;
        this.permissions = permissions;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getPermissions() { return permissions; }
    public void setPermissions(String permissions) { this.permissions = permissions; }
    
    public LocalDateTime getCreationDate() { return creationDate; }
    public void setCreationDate(LocalDateTime creationDate) { this.creationDate = creationDate; }
    
    public LocalDateTime getLastUpdateOn() { return lastUpdateOn; }
    public void setLastUpdateOn(LocalDateTime lastUpdateOn) { this.lastUpdateOn = lastUpdateOn; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
