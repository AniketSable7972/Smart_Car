package com.smartcar.monitoring.dto;

import com.smartcar.monitoring.model.User;
import java.time.LocalDateTime;

public class AuthResponseDto {
    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;
    private String name;
    private String email;
    private User.UserRole role;
    private LocalDateTime expiresAt;
    private String message;
    
    // Default constructor
    public AuthResponseDto() {}
    
    // Constructor for successful authentication
    public AuthResponseDto(String token, User user, LocalDateTime expiresAt) {
        this.token = token;
        this.userId = user.getId();
        this.username = user.getUsername();
        this.name = user.getName();
        this.email = user.getEmail();
        this.role = user.getRole();
        this.expiresAt = expiresAt;
        this.message = "Authentication successful";
    }
    
    // Constructor for failed authentication
    public AuthResponseDto(String message) {
        this.message = message;
    }
    
    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public User.UserRole getRole() { return role; }
    public void setRole(User.UserRole role) { this.role = role; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
