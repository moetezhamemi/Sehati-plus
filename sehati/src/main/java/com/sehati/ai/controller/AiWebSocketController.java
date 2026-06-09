package com.sehati.ai.controller;

import com.sehati.ai.service.ChatbotAiService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AiWebSocketController {

    private final ChatbotAiService chatbotAiService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/ai.chat.{conversationId}")
    public void handleChatMessage(@DestinationVariable Long conversationId, @Payload Map<String, String> payload) {
        String userMessage = payload.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }

        Flux<String> aiResponseStream = chatbotAiService.processUserMessage(conversationId, userMessage);

        aiResponseStream.subscribe(
                token -> {
                    // Send each token to the client
                    messagingTemplate.convertAndSend("/queue/ai-chat/" + conversationId, 
                            (Object) Collections.singletonMap("token", token));
                },
                error -> {
                    // Handle error
                    messagingTemplate.convertAndSend("/queue/ai-chat/" + conversationId, 
                            (Object) Collections.singletonMap("error", error.getMessage()));
                },
                () -> {
                    // Signal completion
                    messagingTemplate.convertAndSend("/queue/ai-chat/" + conversationId, 
                            (Object) Collections.singletonMap("status", "COMPLETED"));
                }
        );
    }
}
