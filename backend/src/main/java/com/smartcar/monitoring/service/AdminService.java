package com.smartcar.monitoring.service;

import com.smartcar.monitoring.model.Admin;
import com.smartcar.monitoring.model.User;
import com.smartcar.monitoring.repository.AdminRepository;
import com.smartcar.monitoring.repository.UserRepository;
import com.smartcar.monitoring.exception.AdminNotFoundException;
import com.smartcar.monitoring.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdminService {
    
    @Autowired
    private AdminRepository adminRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    // Create new admin
    public Admin createAdmin(Admin admin) {
        // Verify user exists and is an ADMIN
        User user = userRepository.findById(admin.getUser().getId())
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + admin.getUser().getId()));
        
        if (user.getRole() != User.UserRole.ADMIN) {
            throw new IllegalArgumentException("User must have ADMIN role");
        }
        
        admin.setCreationDate(LocalDateTime.now());
        admin.setLastUpdateOn(LocalDateTime.now());
        admin.setIsActive(true);
        
        return adminRepository.save(admin);
    }
    
    // Get admin by ID
    public Admin getAdminById(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new AdminNotFoundException("Admin not found with ID: " + id));
    }
    
    // Get admin by user ID
    public Optional<Admin> getAdminByUserId(Long userId) {
        return adminRepository.findByUserId(userId);
    }
    
    // Get all active admins
    public List<Admin> getAllActiveAdmins() {
        return adminRepository.findByIsActiveTrue();
    }
    
    // Get admins by permissions
    public List<Admin> getAdminsByPermissions(String permission) {
        return adminRepository.findByPermissionsContainingAndIsActiveTrue(permission);
    }
    
    // Get admins created in date range
    public List<Admin> getAdminsCreatedBetween(LocalDateTime startDate, LocalDateTime endDate) {
        return adminRepository.findAdminsCreatedBetween(startDate, endDate);
    }
    
    // Update admin
    public Admin updateAdmin(Long id, Admin adminDetails) {
        Admin admin = getAdminById(id);
        
        admin.setPermissions(adminDetails.getPermissions());
        admin.setLastUpdateOn(LocalDateTime.now());
        
        return adminRepository.save(admin);
    }
    
    // Update admin permissions
    public Admin updateAdminPermissions(Long id, String permissions) {
        Admin admin = getAdminById(id);
        admin.setPermissions(permissions);
        admin.setLastUpdateOn(LocalDateTime.now());
        return adminRepository.save(admin);
    }
    
    // Add permission to admin
    public Admin addPermissionToAdmin(Long id, String permission) {
        Admin admin = getAdminById(id);
        String currentPermissions = admin.getPermissions();
        
        if (currentPermissions == null || currentPermissions.isEmpty()) {
            currentPermissions = permission;
        } else if (!currentPermissions.contains(permission)) {
            currentPermissions += "," + permission;
        }
        
        admin.setPermissions(currentPermissions);
        admin.setLastUpdateOn(LocalDateTime.now());
        return adminRepository.save(admin);
    }
    
    // Remove permission from admin
    public Admin removePermissionFromAdmin(Long id, String permission) {
        Admin admin = getAdminById(id);
        String currentPermissions = admin.getPermissions();
        
        if (currentPermissions != null && currentPermissions.contains(permission)) {
            currentPermissions = currentPermissions.replace(permission, "").replace(",,", ",");
            if (currentPermissions.startsWith(",")) {
                currentPermissions = currentPermissions.substring(1);
            }
            if (currentPermissions.endsWith(",")) {
                currentPermissions = currentPermissions.substring(0, currentPermissions.length() - 1);
            }
            admin.setPermissions(currentPermissions);
            admin.setLastUpdateOn(LocalDateTime.now());
        }
        
        return adminRepository.save(admin);
    }
    
    // Check if admin has specific permission
    public boolean hasPermission(Long adminId, String permission) {
        Admin admin = getAdminById(adminId);
        return admin.getPermissions() != null && admin.getPermissions().contains(permission);
    }
    
    // Soft delete admin
    public void deactivateAdmin(Long id) {
        Admin admin = getAdminById(id);
        admin.setIsActive(false);
        admin.setLastUpdateOn(LocalDateTime.now());
        adminRepository.save(admin);
    }
    
    // Reactivate admin
    public void reactivateAdmin(Long id) {
        Admin admin = getAdminById(id);
        admin.setIsActive(true);
        admin.setLastUpdateOn(LocalDateTime.now());
        adminRepository.save(admin);
    }
    
    // Count active admins
    public long countActiveAdmins() {
        return adminRepository.countByIsActiveTrue();
    }
    
    // Count admins with specific permission
    public long countAdminsWithPermission(String permission) {
        return adminRepository.countByPermissionsContainingAndIsActiveTrue(permission);
    }
    
    // Get admin statistics
    public AdminStatistics getAdminStatistics() {
        AdminStatistics stats = new AdminStatistics();
        
        stats.setTotalAdmins(countActiveAdmins());
        stats.setAdminsWithUserManagement(countAdminsWithPermission("USER_MANAGEMENT"));
        stats.setAdminsWithCarManagement(countAdminsWithPermission("CAR_MANAGEMENT"));
        stats.setAdminsWithAlertManagement(countAdminsWithPermission("ALERT_MANAGEMENT"));
        stats.setAdminsWithSystemAccess(countAdminsWithPermission("SYSTEM_ACCESS"));
        
        return stats;
    }
    
    // Inner class for admin statistics
    public static class AdminStatistics {
        private long totalAdmins;
        private long adminsWithUserManagement;
        private long adminsWithCarManagement;
        private long adminsWithAlertManagement;
        private long adminsWithSystemAccess;
        
        // Getters and setters
        public long getTotalAdmins() { return totalAdmins; }
        public void setTotalAdmins(long totalAdmins) { this.totalAdmins = totalAdmins; }
        
        public long getAdminsWithUserManagement() { return adminsWithUserManagement; }
        public void setAdminsWithUserManagement(long adminsWithUserManagement) { this.adminsWithUserManagement = adminsWithUserManagement; }
        
        public long getAdminsWithCarManagement() { return adminsWithCarManagement; }
        public void setAdminsWithCarManagement(long adminsWithCarManagement) { this.adminsWithCarManagement = adminsWithCarManagement; }
        
        public long getAdminsWithAlertManagement() { return adminsWithAlertManagement; }
        public void setAdminsWithAlertManagement(long adminsWithAlertManagement) { this.adminsWithAlertManagement = adminsWithAlertManagement; }
        
        public long getAdminsWithSystemAccess() { return adminsWithSystemAccess; }
        public void setAdminsWithSystemAccess(long adminsWithSystemAccess) { this.adminsWithSystemAccess = adminsWithSystemAccess; }
    }
}
