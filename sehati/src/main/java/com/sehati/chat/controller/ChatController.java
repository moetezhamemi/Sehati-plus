package com.sehati.chat.controller;

import com.sehati.auth.entities.User;
import com.sehati.auth.security.UserDetailsImpl;
import com.sehati.chat.dto.MessageRequest;
import com.sehati.chat.dto.MessageResponse;
import com.sehati.chat.dto.CounterpartDTO;
import com.sehati.chat.service.ChatService;
import com.sehati.common.dto.ApiResponse;
import com.sehati.common.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final CloudinaryService cloudinaryService;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping("/messages")
    public ResponseEntity<ApiResponse<List<MessageResponse>>> getConversation(@AuthenticationPrincipal UserDetailsImpl user) {
        List<MessageResponse> messages = chatService.getConversation(user.getId());
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/counterpart")
    public ResponseEntity<ApiResponse<CounterpartDTO>> getCounterpartInfo(@AuthenticationPrincipal UserDetailsImpl user) {
        CounterpartDTO counterpart = chatService.getCounterpartInfo(user.getId());
        return ResponseEntity.ok(ApiResponse.success(counterpart));
    }

    @PostMapping("/messages")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(@AuthenticationPrincipal UserDetailsImpl user, @RequestBody MessageRequest request) {
        MessageResponse response = chatService.sendMessage(user.getId(), request);
        
        // Push message to receiver via WebSocket
        messagingTemplate.convertAndSendToUser(
                response.getReceiverId().toString(), 
                "/queue/messages", 
                response
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/messages/read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(@AuthenticationPrincipal UserDetailsImpl user) {
        chatService.markMessagesAsRead(user.getId());
        
        // Notify counterpart that messages were read
        try {
            User counterpart = chatService.getCounterpart(user.getId());
            messagingTemplate.convertAndSendToUser(
                    counterpart.getId().toString(),
                    "/queue/read-receipts",
                    user.getId()
            );
        } catch (Exception e) {
            // ignore
        }
        
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    @DeleteMapping("/messages/{id}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteMessage(@AuthenticationPrincipal UserDetailsImpl user, @PathVariable Long id) {
        MessageResponse response = chatService.deleteMessage(id, user.getId());
        
        // Notify receiver of deletion
        messagingTemplate.convertAndSendToUser(
                response.getReceiverId().toString(), 
                "/queue/messages", 
                response
        );
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/upload-audio")
    public ResponseEntity<ApiResponse<String>> uploadAudio(@RequestParam("file") MultipartFile file) {
        String url = cloudinaryService.uploadAudio(file);
        return ResponseEntity.ok(ApiResponse.success(url));
    }
}
