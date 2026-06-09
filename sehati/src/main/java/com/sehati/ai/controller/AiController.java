package com.sehati.ai.controller;

import com.sehati.ai.service.AiService;
import com.sehati.ai.entities.AiConversation;
import com.sehati.ai.entities.AiMessage;
import com.sehati.ai.repositories.AiConversationRepository;
import com.sehati.ai.repositories.AiMessageRepository;
import com.sehati.patient.entities.Patient;
import com.sehati.patient.repository.PatientRepository;
import com.sehati.auth.entities.User;
import com.sehati.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;

    @PostMapping("/transcribe")
    @PreAuthorize("hasAnyAuthority('MEDECIN', 'PATIENT')")
    public ResponseEntity<Map<String, String>> transcribeAudio(@RequestParam("file") MultipartFile file) {
        try {
            String transcribedText = aiService.transcribeAudio(file);
            return ResponseEntity.ok(Collections.singletonMap("text", transcribedText));
        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Erreur lors de la lecture du fichier audio: " + e.getMessage()));
        } catch (RestClientResponseException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Erreur OpenAI (" + e.getStatusCode() + "): " + e.getResponseBodyAsString()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Erreur lors de la transcription: " + e.getMessage()));
        }
    }

    @PostMapping("/conversation")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<AiConversation> startConversation(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        AiConversation conversation = new AiConversation();
        conversation.setPatient(patient);
        AiConversation saved = conversationRepository.save(conversation);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/conversations")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<java.util.List<AiConversation>> getConversations(Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        return ResponseEntity.ok(conversationRepository.findByPatientOrderByStartedAtDesc(patient));
    }

    @GetMapping("/conversation/{id}/messages")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<java.util.List<AiMessage>> getConversationMessages(@PathVariable Long id) {
        AiConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        return ResponseEntity.ok(messageRepository.findByConversationOrderByTimestampAsc(conversation));
    }

    @DeleteMapping("/conversation/{id}")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id, Authentication authentication) {
        String userEmail = authentication.getName();
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User non trouvé"));
        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Patient non trouvé"));

        AiConversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));

        // Security check: patient can only delete their own conversations
        if (!conversation.getPatient().getId().equals(patient.getId())) {
            return ResponseEntity.status(403).build();
        }

        messageRepository.deleteAll(messageRepository.findByConversationOrderByTimestampAsc(conversation));
        conversationRepository.delete(conversation);
        return ResponseEntity.noContent().build();
    }
}
