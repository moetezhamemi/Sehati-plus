package com.sehati.support.service;

import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.support.dto.ContactMessageRequestDTO;
import com.sehati.support.entity.ContactMessage;
import com.sehati.support.entity.MessageStatus;
import com.sehati.support.repository.ContactMessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ContactMessageService {

    @Autowired
    private ContactMessageRepository contactMessageRepository;

    public ContactMessage createMessage(ContactMessageRequestDTO dto) {
        ContactMessage message = ContactMessage.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .subject(dto.getSubject())
                .message(dto.getMessage())
                .status(MessageStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        // Check if user is authenticated
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal() instanceof String && authentication.getPrincipal().equals("anonymousUser"))) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetailsImpl) {
                Long userId = ((UserDetailsImpl) principal).getId();
                message.setUserId(userId);
            }
        }

        return contactMessageRepository.save(message);
    }
}
