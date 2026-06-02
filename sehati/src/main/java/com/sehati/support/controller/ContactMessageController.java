package com.sehati.support.controller;

import com.sehati.support.dto.ContactMessageRequestDTO;
import com.sehati.support.entity.ContactMessage;
import com.sehati.support.service.ContactMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/support")
public class ContactMessageController {

    @Autowired
    private ContactMessageService contactMessageService;

    @PostMapping("/contact")
    public ResponseEntity<?> submitContactMessage(@RequestBody ContactMessageRequestDTO dto) {
        try {
            ContactMessage savedMessage = contactMessageService.createMessage(dto);
            return ResponseEntity.ok(savedMessage);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error submitting message: " + e.getMessage());
        }
    }
}
