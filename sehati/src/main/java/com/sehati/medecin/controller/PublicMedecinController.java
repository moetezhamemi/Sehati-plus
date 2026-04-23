package com.sehati.medecin.controller;

import com.sehati.medecin.dto.MedecinDetailDTO;
import com.sehati.medecin.dto.MedecinSummaryDTO;
import com.sehati.medecin.service.MedecinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/medecins")
@RequiredArgsConstructor
public class PublicMedecinController {

    private final MedecinService medecinService;

    @GetMapping
    public ResponseEntity<Page<MedecinSummaryDTO>> getValidatedMedecins(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ville,
            @RequestParam(required = false) String specialite) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(medecinService.searchDoctors(search, ville, specialite, pageable));
    }

    @GetMapping("/filters")
    public ResponseEntity<java.util.Map<String, java.util.List<String>>> getFilters() {
        java.util.Map<String, java.util.List<String>> filters = new java.util.HashMap<>();
        filters.put("villes", medecinService.getDistinctVilles());
        filters.put("specialites", medecinService.getDistinctSpecialites());
        return ResponseEntity.ok(filters);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MedecinDetailDTO> getMedecinDetail(@PathVariable Long id) {
        return ResponseEntity.ok(medecinService.getMedecinDetail(id));
    }
}
