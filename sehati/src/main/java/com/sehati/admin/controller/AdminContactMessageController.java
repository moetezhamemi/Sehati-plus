package com.sehati.admin.controller;

import com.sehati.support.entity.ContactMessage;
import com.sehati.support.entity.MessageStatus;
import com.sehati.support.service.AdminContactMessageService;
import com.sehati.common.dto.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/support/messages")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminContactMessageController {

    private final AdminContactMessageService service;

    public AdminContactMessageController(AdminContactMessageService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getMessages(
            @RequestParam(required = false) MessageStatus status,
            @RequestParam(required = false) String subject,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ContactMessage> messages = service.getMessages(status, subject, pageable);
        
        return ResponseEntity.ok(ApiResponse.success("Success", messages));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getMessageById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Success", service.getMessageById(id)));
    }

    @PutMapping("/{id}/reply")
    public ResponseEntity<ApiResponse> replyToMessage(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String replyText = payload.get("replyText");
        if (replyText == null || replyText.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Reply text is required"));
        }
        
        ContactMessage msg = service.replyToMessage(id, replyText);
        return ResponseEntity.ok(ApiResponse.success("Reply sent successfully", msg));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse> deleteMessage(@PathVariable Long id) {
        service.deleteMessage(id);
        return ResponseEntity.ok(ApiResponse.success("Message deleted successfully", null));
    }
}
