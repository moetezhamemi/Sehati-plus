package com.sehati.chat.dto;

import com.sehati.chat.entities.MessageStatus;
import com.sehati.chat.entities.MessageType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class MessageResponse {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private MessageType type;
    private MessageStatus status;
    private LocalDateTime timestamp;
    private Boolean isDeleted;
}
