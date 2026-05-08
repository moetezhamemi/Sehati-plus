package com.sehati.medecin.controller;

import com.sehati.medecin.dto.SpecialiteDTO;
import com.sehati.medecin.service.SpecialiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/public/specialites")
@RequiredArgsConstructor
public class SpecialiteController {

    private final SpecialiteService specialiteService;

    @GetMapping
    public ResponseEntity<List<SpecialiteDTO>> getAllSpecialites() {
        List<SpecialiteDTO> dtos = specialiteService.getAllSpecialites()
                .stream()
                .map(s -> SpecialiteDTO.builder().id(s.getId()).nom(s.getNom()).build())
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }
}
