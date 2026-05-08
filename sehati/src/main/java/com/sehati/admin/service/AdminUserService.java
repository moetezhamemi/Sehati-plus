package com.sehati.admin.service;

import com.sehati.admin.dto.AdminUserDetailDTO;
import com.sehati.admin.dto.UserAdminProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminUserService {
    Page<UserAdminProjection> getAllUsers(String search, String role, String status, String specialite, Pageable pageable);
    Page<UserAdminProjection> getPendingUsers(String search, String role, String specialite, Pageable pageable);
    AdminUserDetailDTO getUserDetail(Long userId);
    void updateUserStatus(Long userId, boolean enabled);
    void approveRequest(Long userId);
    void rejectRequest(Long userId);
}
