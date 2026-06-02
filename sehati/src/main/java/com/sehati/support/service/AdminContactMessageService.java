package com.sehati.support.service;

import com.sehati.support.entity.ContactMessage;
import com.sehati.support.entity.MessageStatus;
import com.sehati.support.repository.ContactMessageRepository;
import com.sehati.auth.service.EmailService;
import com.sehati.common.exception.BusinessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminContactMessageService {

    private final ContactMessageRepository repository;
    private final EmailService emailService;

    public AdminContactMessageService(ContactMessageRepository repository, EmailService emailService) {
        this.repository = repository;
        this.emailService = emailService;
    }

    public Page<ContactMessage> getMessages(MessageStatus status, String subject, Pageable pageable) {
        return repository.findByFilters(status, subject, pageable);
    }

    @Transactional
    public ContactMessage getMessageById(Long id) {
        ContactMessage msg = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Message not found"));
        
        if (msg.getStatus() == MessageStatus.PENDING) {
            msg.setStatus(MessageStatus.IN_PROGRESS);
            repository.save(msg);
        }
        return msg;
    }

    @Transactional
    public ContactMessage replyToMessage(Long id, String replyText) {
        ContactMessage msg = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Message not found"));
        
        msg.setAdminResponse(replyText);
        msg.setStatus(MessageStatus.RESOLVED);
        msg = repository.save(msg);
        
        emailService.sendSupportReplyEmail(msg.getEmail(), msg.getSubject(), replyText);
        
        return msg;
    }

    @Transactional
    public void deleteMessage(Long id) {
        ContactMessage msg = repository.findById(id)
            .orElseThrow(() -> new BusinessException("Message not found"));
            
        if (msg.getStatus() != MessageStatus.RESOLVED) {
            throw new IllegalStateException("Cannot delete a message that is not resolved");
        }
        
        repository.delete(msg);
    }
}
