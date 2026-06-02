package com.sehati.admin.controller;

import com.sehati.admin.dto.AdminUserDetailDTO;
import com.sehati.admin.dto.UserAdminProjection;
import com.sehati.admin.service.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<UserAdminProjection>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String specialite) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.getAllUsers(search, role, status, specialite, pageable));
    }

    @GetMapping("/requests")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Page<UserAdminProjection>> getRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String specialite) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adminUserService.getPendingUsers(search, role, specialite, pageable));
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AdminUserDetailDTO> getUserDetail(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.getUserDetail(userId));
    }

    @PutMapping("/users/{userId}/status")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long userId, @RequestParam boolean enabled) {
        adminUserService.updateUserStatus(userId, enabled);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{userId}/approve")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> approveRequest(@PathVariable Long userId) {
        adminUserService.approveRequest(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/requests/{userId}/reject")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> rejectRequest(@PathVariable Long userId) {
        adminUserService.rejectRequest(userId);
        return ResponseEntity.ok().build();
    }
}
