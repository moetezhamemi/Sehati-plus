package com.sehati.chat.service;

import com.sehati.auth.entities.User;
import com.sehati.auth.repositories.UserRepository;
import com.sehati.chat.dto.MessageRequest;
import com.sehati.chat.dto.MessageResponse;
import com.sehati.chat.dto.CounterpartDTO;
import com.sehati.chat.entities.Message;
import com.sehati.chat.entities.MessageStatus;
import com.sehati.chat.repository.MessageRepository;
import com.sehati.common.exception.BusinessException;
import com.sehati.medecin.entities.Medecin;
import com.sehati.medecin.repository.MedecinRepository;
import com.sehati.secretaire.entities.MedecinSecretaire;
import com.sehati.secretaire.repository.MedecinSecretaireRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MedecinRepository medecinRepository;
    private final MedecinSecretaireRepository medecinSecretaireRepository;

    public User getCounterpart(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        boolean isMedecin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("MEDECIN"));
        boolean isSecretaire = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("SECRETAIRE"));

        if (isMedecin) {
            Medecin medecin = medecinRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new BusinessException("Médecin introuvable"));
            Optional<MedecinSecretaire> relation = medecinSecretaireRepository.findByMedecinIdAndStatusIn(medecin.getId(), List.of("ACTIVE"));
            if (relation.isPresent()) {
                return relation.get().getSecretaire().getUser();
            }
        } else if (isSecretaire) {
            List<MedecinSecretaire> relations = medecinSecretaireRepository.findBySecretaireUserId(currentUserId);
            Optional<MedecinSecretaire> activeRelation = relations.stream()
                    .filter(r -> "ACTIVE".equals(r.getStatus()))
                    .findFirst();
            if (activeRelation.isPresent()) {
                return activeRelation.get().getMedecin().getUser();
            }
        }

        throw new BusinessException("Aucun interlocuteur actif trouvé pour la conversation.");
    }

    public CounterpartDTO getCounterpartInfo(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new BusinessException("Utilisateur introuvable"));

        boolean isMedecin = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("MEDECIN"));
        boolean isSecretaire = currentUser.getRoles().stream().anyMatch(r -> r.getName().equals("SECRETAIRE"));

        if (isMedecin) {
            Medecin medecin = medecinRepository.findByUserId(currentUserId)
                    .orElseThrow(() -> new BusinessException("Médecin introuvable"));
            Optional<MedecinSecretaire> relation = medecinSecretaireRepository.findByMedecinIdAndStatusIn(medecin.getId(), List.of("ACTIVE"));
            if (relation.isPresent()) {
                com.sehati.secretaire.entities.Secretaire sec = relation.get().getSecretaire();
                return CounterpartDTO.builder()
                        .id(sec.getUser().getId())
                        .nom(sec.getNom())
                        .prenom(sec.getPrenom())
                        .build();
            }
        } else if (isSecretaire) {
            List<MedecinSecretaire> relations = medecinSecretaireRepository.findBySecretaireUserId(currentUserId);
            Optional<MedecinSecretaire> activeRelation = relations.stream()
                    .filter(r -> "ACTIVE".equals(r.getStatus()))
                    .findFirst();
            if (activeRelation.isPresent()) {
                Medecin med = activeRelation.get().getMedecin();
                return CounterpartDTO.builder()
                        .id(med.getUser().getId())
                        .nom(med.getNom())
                        .prenom(med.getPrenom())
                        .build();
            }
        }

        throw new BusinessException("Aucun interlocuteur actif trouvé pour la conversation.");
    }

    public List<MessageResponse> getConversation(Long currentUserId) {
        User counterpart = getCounterpart(currentUserId);
        List<Message> messages = messageRepository.findConversation(currentUserId, counterpart.getId());
        return messages.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional
    public MessageResponse sendMessage(Long senderId, MessageRequest request) {
        User counterpart = getCounterpart(senderId);
        User sender = userRepository.findById(senderId).orElseThrow();

        Message message = Message.builder()
                .sender(sender)
                .receiver(counterpart)
                .content(request.getContent())
                .type(request.getType())
                .status(MessageStatus.ENVOYE)
                .isDeleted(false)
                .build();

        message = messageRepository.save(message);
        return mapToResponse(message);
    }

    @Transactional
    public void markMessagesAsRead(Long currentUserId) {
        User counterpart;
        try {
            counterpart = getCounterpart(currentUserId);
        } catch (BusinessException e) {
            return; // No counterpart, nothing to mark
        }
        messageRepository.markMessagesAsRead(currentUserId, counterpart.getId());
    }

    @Transactional
    public MessageResponse deleteMessage(Long messageId, Long currentUserId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("Message introuvable"));

        if (!message.getSender().getId().equals(currentUserId)) {
            throw new BusinessException("Vous ne pouvez supprimer que vos propres messages");
        }

        message.setIsDeleted(true);
        message = messageRepository.save(message);
        return mapToResponse(message);
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .receiverId(message.getReceiver().getId())
                .content(message.getIsDeleted() ? "Ce message a été supprimé." : message.getContent())
                .type(message.getType())
                .status(message.getStatus())
                .timestamp(message.getTimestamp())
                .isDeleted(message.getIsDeleted())
                .build();
    }
}
