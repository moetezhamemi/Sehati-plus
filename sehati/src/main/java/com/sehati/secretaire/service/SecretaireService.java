package com.sehati.secretaire.service;

import com.sehati.secretaire.dto.AddSecretaireRequest;
import com.sehati.secretaire.dto.SecretaireResponse;
import com.sehati.secretaire.dto.SecretaireStatusResponse;

public interface SecretaireService {
    SecretaireResponse getMedecinSecretaire(Long userId);
    SecretaireResponse inviteSecretaire(Long userId, AddSecretaireRequest request);
    void removeSecretaire(Long userId);
    SecretaireStatusResponse getSecretaireStatus(Long userId);
}
