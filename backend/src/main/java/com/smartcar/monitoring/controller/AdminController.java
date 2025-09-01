package com.smartcar.monitoring.controller;

import com.smartcar.monitoring.dto.*;
import com.smartcar.monitoring.model.Admin;
import com.smartcar.monitoring.model.User;
import com.smartcar.monitoring.service.AdminService;
import com.smartcar.monitoring.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admins")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private AdminService adminService;
    
    @Autowired
    private UserService userService;

    // POST /api/admins - Create new admin
    @PostMapping
    public ResponseEntity<ApiResponseDto<AdminDto>> createAdmin(@Valid @RequestBody AdminDto adminDto) {
        try {
            User user = userService.getUserById(adminDto.getUserId());
            Admin admin = new Admin();
            admin.setUser(user);
            admin.setPermissions(adminDto.getPermissions());
            
            Admin createdAdmin = adminService.createAdmin(admin);
            AdminDto createdAdminDto = new AdminDto(createdAdmin);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponseDto.success("Admin created successfully", createdAdminDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to create admin: " + e.getMessage()));
        }
    }

    // GET /api/admins - Get all active admins
    @GetMapping
    public ResponseEntity<ApiResponseDto<List<AdminDto>>> getAllAdmins() {
        try {
            List<Admin> admins = adminService.getAllActiveAdmins();
            List<AdminDto> adminDtos = admins.stream()
                    .map(AdminDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Admins retrieved successfully", adminDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve admins: " + e.getMessage()));
        }
    }

    // GET /api/admins/{id} - Get admin by ID
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<AdminDto>> getAdminById(@PathVariable Long id) {
        try {
            Admin admin = adminService.getAdminById(id);
            AdminDto adminDto = new AdminDto(admin);
            return ResponseEntity.ok(ApiResponseDto.success("Admin retrieved successfully", adminDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Admin not found: " + e.getMessage()));
        }
    }

    // GET /api/admins/user/{userId} - Get admin by user ID
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponseDto<AdminDto>> getAdminByUserId(@PathVariable Long userId) {
        try {
            Admin admin = adminService.getAdminByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Admin not found for user ID: " + userId));
            AdminDto adminDto = new AdminDto(admin);
            return ResponseEntity.ok(ApiResponseDto.success("Admin retrieved successfully", adminDto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDto.error("Admin not found: " + e.getMessage()));
        }
    }

    // PUT /api/admins/{id} - Update admin
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponseDto<AdminDto>> updateAdmin(@PathVariable Long id, @Valid @RequestBody AdminDto adminDto) {
        try {
            Admin admin = new Admin();
            admin.setPermissions(adminDto.getPermissions());
            
            Admin updatedAdmin = adminService.updateAdmin(id, admin);
            AdminDto updatedAdminDto = new AdminDto(updatedAdmin);
            return ResponseEntity.ok(ApiResponseDto.success("Admin updated successfully", updatedAdminDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to update admin: " + e.getMessage()));
        }
    }

    // DELETE /api/admins/{id} - Soft delete admin (deactivate)
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponseDto<String>> deactivateAdmin(@PathVariable Long id) {
        try {
            adminService.deactivateAdmin(id);
            return ResponseEntity.ok(ApiResponseDto.success("Admin deactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to deactivate admin: " + e.getMessage()));
        }
    }

    // PUT /api/admins/{id}/reactivate - Reactivate admin
    @PutMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponseDto<String>> reactivateAdmin(@PathVariable Long id) {
        try {
            adminService.reactivateAdmin(id);
            return ResponseEntity.ok(ApiResponseDto.success("Admin reactivated successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to reactivate admin: " + e.getMessage()));
        }
    }

    // GET /api/admins/permissions/{permissions} - Get admins by permissions
    @GetMapping("/permissions/{permissions}")
    public ResponseEntity<ApiResponseDto<List<AdminDto>>> getAdminsByPermissions(@PathVariable String permissions) {
        try {
            List<Admin> admins = adminService.getAdminsByPermissions(permissions);
            List<AdminDto> adminDtos = admins.stream()
                    .map(AdminDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Admins retrieved successfully", adminDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve admins: " + e.getMessage()));
        }
    }

    // GET /api/admins/stats/count - Get admin count statistics
    @GetMapping("/stats/count")
    public ResponseEntity<ApiResponseDto<Object>> getAdminCountStats() {
        try {
            long totalAdmins = adminService.countActiveAdmins();
            
            class AdminStats {
                public final long totalAdmins;
                public AdminStats(long totalAdmins) {
                    this.totalAdmins = totalAdmins;
                }
            }
            AdminStats stats = new AdminStats(totalAdmins);
            return ResponseEntity.ok(ApiResponseDto.success("Admin statistics retrieved successfully", stats));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to retrieve admin statistics: " + e.getMessage()));
        }
    }

    // PUT /api/admins/{id}/update-permissions - Update admin permissions
    @PutMapping("/{id}/update-permissions")
    public ResponseEntity<ApiResponseDto<AdminDto>> updateAdminPermissions(@PathVariable Long id, 
                                                                          @RequestParam String newPermissions) {
        try {
            Admin admin = adminService.updateAdminPermissions(id, newPermissions);
            AdminDto adminDto = new AdminDto(admin);
            return ResponseEntity.ok(ApiResponseDto.success("Admin permissions updated successfully", adminDto));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponseDto.error("Failed to update admin permissions: " + e.getMessage()));
        }
    }

    // GET /api/admins/search/permissions - Search admins by permissions
    @GetMapping("/search/permissions")
    public ResponseEntity<ApiResponseDto<List<AdminDto>>> searchAdminsByPermissions(@RequestParam String permissionsPattern) {
        try {
            List<Admin> admins = adminService.getAdminsByPermissions(permissionsPattern);
            List<AdminDto> adminDtos = admins.stream()
                    .map(AdminDto::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(ApiResponseDto.success("Admins found successfully", adminDtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponseDto.error("Failed to search admins: " + e.getMessage()));
        }
    }
}
