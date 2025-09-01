package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.*;
import com.smartcar.monitoring.model.User;
import com.smartcar.monitoring.model.Driver;
import com.smartcar.monitoring.service.UserService;
import com.smartcar.monitoring.service.DriverService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private DriverService driverService;

    // POST /api/users/register - Register new user
    @PostMapping("/register")
    public ResponseEntity<ApiResponseDto<UserDto>> registerUser(@Valid @RequestBody UserDto userDto) {
        try {
            // Force role to DRIVER for all frontend registrations
            userDto.setRole(User.UserRole.DRIVER);

            User user = userService.createUser(userDto.toEntity());

            // Auto-create Driver with assignedCarId = null
            Driver driver = new Driver();
            driver.setUser(user);
            driver.setAssignedCarId(null);
            driverService.createDriver(driver);

            UserDto createdUserDto = new UserDto(user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("User registered successfully", createdUserDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to register user: " + e.getMessage()));
        }
    }

    // POST /api/users/login - User login
    @PostMapping("/login")
    public ResponseEntity<ApiResponseDto<AuthResponseDto>> loginUser(@Valid @RequestBody LoginDto loginDto) {
        try {
            if (userService.validateCredentials(loginDto.getUsername(), loginDto.getPassword())) {
                User user = userService.getUserByUsername(loginDto.getUsername()).orElse(null);
                if (user != null) {
                    // TODO: Generate JWT token here
                    String token = "dummy-token-" + System.currentTimeMillis();
                    LocalDateTime expiresAt = LocalDateTime.now().plusHours(24);
                    AuthResponseDto authResponse = new AuthResponseDto(token, user, expiresAt);
                    return ResponseEntity.ok(ApiResponseDto.success("Login successful", authResponse));
                }
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponseDto.error("Invalid credentials"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Login failed: " + e.getMessage()));
        }
    }

    // GET /api/users - Get all active users
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<UserDto>>> getAllUsers() {
        try {
            List<User> users = userService.getAllActiveUsers();
            List<UserDto> userDtos = users.stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Users retrieved successfully", userDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    // GET /api/users/{id} - Get user by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserDto>> getUserById(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id);
            UserDto userDto = new UserDto(user);
            return ResponseEntity.ok(ApiResponseDto.success("User retrieved successfully", userDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("User not found: " + e.getMessage()));
        }
    }

    // GET /api/users/role/{role} - Get users by role
    @GetMapping("/role/{role}")
    public ResponseEntity<ApiResponseDto<List<UserDto>>> getUsersByRole(@PathVariable String role) {
        try {
            User.UserRole userRole = User.UserRole.valueOf(role.toUpperCase());
            List<User> users = userService.getUsersByRole(userRole);
            List<UserDto> userDtos = users.stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Users retrieved successfully", userDtos));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Invalid role: " + role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve users: " + e.getMessage()));
        }
    }

    // PUT /api/users/{id} - Update user
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<UserDto>> updateUser(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        try {
            User updatedUser = userService.updateUser(id, userDto.toEntity());
            UserDto updatedUserDto = new UserDto(updatedUser);
            return ResponseEntity.ok(ApiResponseDto.success("User updated successfully", updatedUserDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to update user: " + e.getMessage()));
        }
    }

    // DELETE /api/users/{id} - Soft delete user (deactivate)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deactivateUser(@PathVariable Long id) {
        try {
            userService.deactivateUser(id);
            return ResponseEntity.ok(ApiResponseDto.success("User deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to deactivate user: " + e.getMessage()));
        }
    }

    // PUT /api/users/{id}/reactivate - Reactivate user
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponseDto<String>> reactivateUser(@PathVariable Long id) {
        try {
            userService.reactivateUser(id);
            return ResponseEntity.ok(ApiResponseDto.success("User reactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to reactivate user: " + e.getMessage()));
        }
    }

    // GET /api/users/search/name - Search users by name pattern
    @GetMapping("/search/name")
    public ResponseEntity<ApiResponseDto<List<UserDto>>> searchUsersByName(@RequestParam String namePattern) {
        try {
            List<User> users = userService.getUsersByNamePattern(namePattern);
            List<UserDto> userDtos = users.stream()
                    .map(UserDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Users found successfully", userDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to search users: " + e.getMessage()));
        }
    }

    // GET /api/users/stats/count - Get user count statistics
    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponseDto<Object>> getUserCountStats() {
        try {
            long totalUsers = userService.countTotalActiveUsers();
            long adminUsers = userService.countActiveUsersByRole(User.UserRole.ADMIN);
            long driverUsers = userService.countActiveUsersByRole(User.UserRole.DRIVER);

            class UserStats {
                public final long totalUsers;
                public final long adminUsers;
                public final long driverUsers;
                public UserStats(long totalUsers, long adminUsers, long driverUsers) {
                    this.totalUsers = totalUsers;
                    this.adminUsers = adminUsers;
                    this.driverUsers = driverUsers;
                }
            }
            UserStats stats = new UserStats(totalUsers, adminUsers, driverUsers);
            return ResponseEntity.ok(ApiResponseDto.success("User statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve user statistics: " + e.getMessage()));
        }
    }
}
