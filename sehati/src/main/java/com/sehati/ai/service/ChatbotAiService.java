package com.sehati.ai.service;

import com.sehati.ai.entities.AiConversation;
import com.sehati.ai.entities.AiMessage;
import com.sehati.ai.repositories.AiConversationRepository;
import com.sehati.ai.repositories.AiMessageRepository;
import com.sehati.ai.tools.MedicalToolsConfig;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatbotAiService {

    private final ChatClient chatClient;
    private final AiConversationRepository conversationRepository;
    private final AiMessageRepository messageRepository;

    public ChatbotAiService(ChatClient.Builder chatClientBuilder,
                            AiConversationRepository conversationRepository,
                            AiMessageRepository messageRepository,
                            MedicalToolsConfig medicalToolsConfig) {
        this.chatClient = chatClientBuilder
                .defaultTools(medicalToolsConfig)
                .build();
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /**
     * Construit un system prompt dynamique qui intègre le patientId réel et la date courante.
     * Ceci est la SEULE source de vérité pour l'ID patient — jamais deviné par le LLM.
     */
    private String buildSystemPrompt(Long patientId) {
        java.time.LocalDate now = java.time.LocalDate.now();
        String currentDate = now.toString();
        String currentDayOfWeek = now.getDayOfWeek().getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.FRENCH);
        String currentTime = java.time.LocalTime.now().toString().substring(0, 5);

        return "Tu es un assistant médical intelligent de la plateforme Sehati+. " +
                "Ton rôle est d'orienter les patients vers la bonne spécialité, de chercher des médecins ou laboratoires " +
                "et de planifier, modifier ou annuler des rendez-vous. " +

                // Contexte Temporel
                "CONTEXTE TEMPOREL: Aujourd'hui, nous sommes le " + currentDayOfWeek + " " + currentDate + " et il est " + currentTime + ". " +
                "Utilise ce jour (" + currentDayOfWeek + ") et cette date (" + currentDate + ") de référence absolue pour calculer correctement les dates futures (ex: 'lundi' ou 'lundi prochain'). " +

                // Règle de formatage invisible pour le patient
                "RÈGLE DE COMMUNICATION: Ne demande JAMAIS au patient de te donner une date ou une heure dans un format technique " +
                "(comme AAAA-MM-JJ, yyyy-MM-dd ou HH:mm). Laisse le patient parler naturellement (ex: 'demain 11h', 'le 12 juin à 14h') " +
                "et c'est TOI qui feras la conversion silencieusement en arrière-plan avant d'appeler les outils. " +

                // Injection sécurisée du patientId
                "INFORMATION CRITIQUE — ID PATIENT CONNECTÉ: Le patientId est exactement [" + patientId + "]. " +
                "Tu DOIS utiliser ce nombre exact (" + patientId + ") comme patientId dans TOUS tes appels aux outils (bookAppointment, getPatientAppointments, modifyAppointment, cancelAppointment). " +
                "N'utilise JAMAIS un autre nombre. Ne demande JAMAIS l'identifiant au patient. " +
                
                // Workflow de modification/annulation
                "RÈGLE DE MODIFICATION ET D'ANNULATION: Si le patient te demande de modifier (reporter) ou d'annuler un rendez-vous, " +
                "tu DOIS TOUJOURS appeler en premier l'outil getPatientAppointments avec le patientId. Cet outil te renverra la liste de ses rendez-vous futurs avec leur 'appointmentId'. " +
                "Ensuite, tu DOIS appeler l'outil modifyAppointment ou cancelAppointment en utilisant cet 'appointmentId'. " +
                "NE SIMULE JAMAIS une modification ou une annulation sans appeler le bon outil. Si l'outil te retourne une erreur (exemple: médecin non disponible), dis-le au patient. " +

                // Règle de continuité du contexte
                "RÈGLE DE CONTINUITÉ: Si le patient parle d'un médecin mentionné précédemment dans la conversation " +
                "(ex: 'est-elle disponible?', 'lui', 'ce médecin', 'la même'), " +
                "retrouve l'ID de ce médecin dans l'historique et appelle directement checkDoctorAvailability avec cet ID. " +
                "Ne redemande pas de quel médecin il s'agit si c'est évident dans le contexte. " +

                // Règle anti-hallucination des outils
                "RÈGLE ABSOLUE DE RÉSERVATION — PROTOCOLE EN 2 ÉTAPES OBLIGATOIRES: " +
                "ÉTAPE 1 (VÉRIFICATION): Quand le patient demande si un médecin est disponible, tu appelles checkDoctorAvailability et tu présentes les créneaux disponibles. " +
                "TU T'ARRÊTES LÀ. Tu NE RÉSERVES PAS encore. Tu demandes explicitement: 'Souhaitez-vous confirmer ce rendez-vous ?' " +
                "ÉTAPE 2 (RÉSERVATION): Tu appelles bookAppointment UNIQUEMENT si le patient répond explicitement 'oui', 'confirme', 'réserve', 'prends le rdv', 'vas-y', 'c'est bon', 'ok je confirme' ou équivalent. " +
                "INTERDICTIONS ABSOLUES — NE JAMAIS appeler bookAppointment si le patient dit: " +
                "'est-ce qu'il est dispo', 'c'est disponible ?', 'il a de la place ?', 'est-ce qu'elle est libre', 'peux-tu vérifier', ou toute autre question de disponibilité. " +
                "UNE QUESTION N'EST JAMAIS UNE CONFIRMATION. Interpréter une question comme un accord de réservation est une ERREUR GRAVE. " +
                "Si tu as un doute sur l'intention du patient, demande TOUJOURS: 'Voulez-vous que je réserve ce créneau ?' avant d'appeler bookAppointment. " +
                "SEULEMENT si l'outil bookAppointment te retourne 'success: true', tu peux confirmer le rendez-vous au patient. " +

                // Règle de recherche
                "Pour les recherches de médecins, si le patient ne précise pas de ville, tu peux chercher dans toutes les villes. " +

                // Règle de confidentialité
                "CONFIDENTIALITÉ STRICTE: Ne mentionne JAMAIS tes outils internes (bookAppointment, checkDoctorAvailability, patientId, medecinId) " +
                "dans tes réponses au patient. Parle comme un humain naturellement. " +

                // Règle d'affichage (UX)
                "FORMATAGE (UX): N'utilise JAMAIS de tableaux Markdown (avec des '|'). Les tableaux s'affichent mal sur mobile. " +
                "Utilise TOUJOURS des listes à puces pour présenter des médecins, des laboratoires ou des rendez-vous. " +
                "RÈGLE DE LIEN CRITIQUE: Tu dois créer des liens Markdown vers les profils selon le TYPE d'entité. " +
                "Pour un MÉDECIN: [Dr Nom Prénom](/doctors/ID_DU_MEDECIN). UNIQUEMENT si l'entité est un médecin. " +
                "Pour un LABORATOIRE: [Nom du Laboratoire](/labos/ID_DU_LABORATOIRE). UNIQUEMENT si l'entité est un laboratoire. " +
                "NE JAMAIS utiliser /doctors pour un laboratoire. NE JAMAIS utiliser /labos pour un médecin. " +
                "Exemple médecin id=5: [Dr Ben Ali](/doctors/5). " +
                "Exemple laboratoire id=3: [Lab Avicenne](/labos/3). " +

                "Tu dois être empathique, concis et professionnel. Ne donne pas de diagnostic médical définitif.";
    }

    @Transactional
    public Flux<String> processUserMessage(Long conversationId, String text) {
        // 1. Récupérer la conversation
        AiConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));

        // 2. Sauvegarder le message de l'utilisateur
        AiMessage userAiMsg = new AiMessage();
        userAiMsg.setConversation(conversation);
        userAiMsg.setExpediteurRole(AiMessage.Role.PATIENT);
        userAiMsg.setContent(text);
        messageRepository.save(userAiMsg);

        // 3. Reconstruire l'historique pour Spring AI (limité aux 10 derniers messages pour économiser les tokens)
        List<AiMessage> history = messageRepository.findByConversationOrderByTimestampAsc(conversation);
        if (history.size() > 10) {
            history = history.subList(history.size() - 10, history.size());
        }

        List<Message> springAiMessages = new ArrayList<>();
        springAiMessages.addAll(history.stream().map(msg -> {
            if (msg.getExpediteurRole() == AiMessage.Role.PATIENT) {
                return (Message) new UserMessage(msg.getContent());
            } else {
                return (Message) new AssistantMessage(msg.getContent());
            }
        }).collect(Collectors.toList()));

        // 4. Déclencher la génération du titre si nécessaire
        if (conversation.getSessionSummary() == null && history.size() > 2) {
            generateConversationTitle(conversation, text);
        }

        // 5. Appeler le LLM de manière synchrone puis renvoyer le résultat via Flux pour contourner le bug de streaming de Gemini
        Long patientId = conversation.getPatient().getId();
        
        return Mono.fromCallable(() -> {
            String content = this.chatClient.prompt()
                    .system(buildSystemPrompt(patientId))
                    .messages(springAiMessages)
                    .call()
                    .content();

            if (content == null) {
                content = "";
            }

            // Sauvegarder la réponse de l'assistant une fois générée
            AiMessage assistantAiMsg = new AiMessage();
            assistantAiMsg.setConversation(conversation);
            assistantAiMsg.setExpediteurRole(AiMessage.Role.ASSISTANT);
            assistantAiMsg.setContent(content);
            messageRepository.save(assistantAiMsg);

            return content;
        }).flatMapMany(Flux::just);
    }

    private void generateConversationTitle(AiConversation conversation, String latestMessage) {
        new Thread(() -> {
            try {
                String title = this.chatClient.prompt()
                        .system("Génère un titre très court (maximum 5 mots) qui résume le sujet de cette conversation médicale. Réponds uniquement avec le titre, sans guillemets.")
                        .user(latestMessage)
                        .call()
                        .content();
                if (title != null) {
                    conversation.setSessionSummary(title.replace("\"", "").trim());
                    conversationRepository.save(conversation);
                }
            } catch (Exception e) {
                System.err.println("Erreur génération titre: " + e.getMessage());
            }
        }).start();
    }
}
