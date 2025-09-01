package com.smartcar.monitoring.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Entity
@Table(name = "admins")
public class Admin {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;
    
    @Column(nullable = false)
    @NotNull(message = "Permissions are required")
    private String permissions;
    
    @Column(name = "creation_date", nullable = false)
    private LocalDateTime creationDate;
    
    @Column(name = "last_update_on")
    private LocalDateTime lastUpdateOn;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    // Constructors
    public Admin() {
        this.creationDate = LocalDateTime.now();
        this.lastUpdateOn = LocalDateTime.now();
        this.isActive = true;
    }
    
    public Admin(User user, String permissions) {
        this();
        this.user = user;
        this.permissions = permissions;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getPermissions() {
        return permissions;
    }
    
    public void setPermissions(String permissions) {
        this.permissions = permissions;
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
        return "Admin{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", permissions='" + permissions + '\'' +
                ", isActive=" + isActive +
                '}';
    }
}
