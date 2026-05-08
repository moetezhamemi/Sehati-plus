package com.sehati.chat.dto;

import com.sehati.chat.entities.MessageType;
import lombok.Data;

@Data
public class MessageRequest {
    private String content;
    private MessageType type; // TEXT or AUDIO
}
