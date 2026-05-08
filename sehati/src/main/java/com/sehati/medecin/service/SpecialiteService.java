package com.sehati.medecin.service;

import com.sehati.medecin.entities.Specialite;
import com.sehati.medecin.repository.SpecialiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SpecialiteService {
    private final SpecialiteRepository specialiteRepository;

    public List<Specialite> getAllSpecialites() {
        return specialiteRepository.findAll();
    }

    public Specialite createSpecialite(String nom) {
        if (specialiteRepository.findByNomIgnoreCase(nom).isPresent()) {
            throw new IllegalArgumentException("Cette spécialité existe déjà");
        }
        Specialite specialite = new Specialite();
        specialite.setNom(nom);
        return specialiteRepository.save(specialite);
    }

    public Specialite getOrCreateSpecialite(String nom) {
        if (nom == null || nom.trim().isEmpty()) return null;
        return specialiteRepository.findByNomIgnoreCase(nom.trim())
                .orElseGet(() -> {
                    Specialite s = new Specialite();
                    s.setNom(nom.trim());
                    return specialiteRepository.save(s);
                });
    }
}
