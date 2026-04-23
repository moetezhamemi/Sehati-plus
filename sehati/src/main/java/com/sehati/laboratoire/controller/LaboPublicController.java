package com.sehati.laboratoire.controller;

import com.sehati.laboratoire.dto.LaboDetailDTO;
import com.sehati.laboratoire.dto.LaboSummaryDTO;
import com.sehati.laboratoire.service.LaboQueryService;
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
@RequestMapping("/api/public/labos")
@RequiredArgsConstructor
public class LaboPublicController {

    private final LaboQueryService laboQueryService;

    @GetMapping
    public ResponseEntity<Page<LaboSummaryDTO>> getValidatedLabos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String ville) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(laboQueryService.searchLabos(search, ville, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<LaboDetailDTO> getLaboDetail(@PathVariable Long id) {
        return ResponseEntity.ok(laboQueryService.getLaboDetail(id));
    }
}
