package com.sehati.ai.controller;

import com.sehati.ai.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PostMapping("/transcribe")
    @PreAuthorize("hasAuthority('MEDECIN')")
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
}
