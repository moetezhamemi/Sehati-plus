package com.sehati.appointment.service;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.sehati.appointment.dto.OrdonnanceRequestDTO;
import com.sehati.appointment.dto.DemandeAnalyseRequestDTO;
import com.sehati.appointment.entities.Appointment;
import com.sehati.medecin.entities.Medecin;
import com.sehati.common.service.CloudinaryService;
import com.sehati.patient.entities.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrdonnancePdfService {

    private String formatPhoneNumber(String phone) {
        if (phone == null) return "";
        String cleaned = phone.replaceAll("\\D", "");
        if (cleaned.length() != 8) return phone;
        return cleaned.substring(0, 2) + " " + cleaned.substring(2, 5) + " " + cleaned.substring(5, 8);
    }


    private final org.thymeleaf.TemplateEngine templateEngine;
    private final CloudinaryService cloudinaryService;

    public String generateAndUploadOrdonnance(Appointment appointment, OrdonnanceRequestDTO requestDTO) {
        Medecin medecin = appointment.getMedecin();
        Patient patient = appointment.getPatient();

        if (medecin == null || patient == null) {
            throw new RuntimeException("Données du médecin ou du patient manquantes pour ce rendez-vous");
        }

        // 1. Préparer le contexte Thymeleaf
        Context context = new Context();
        context.setVariable("nomMedecin", medecin.getNom() + " " + medecin.getPrenom());
        context.setVariable("specialiteMedecin", medecin.getSpecialite() != null ? medecin.getSpecialite() : "Médecin");
        context.setVariable("villeMedecin", medecin.getVille() != null ? medecin.getVille() : "");
        context.setVariable("dateAujourdhui", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        context.setVariable("nomPatient", patient.getNom() + " " + patient.getPrenom());
        context.setVariable("medicaments", requestDTO.getMedicaments());
        context.setVariable("signatureMedecin", medecin.getSignatureUrl());
        context.setVariable("cachetMedecin", medecin.getCachetUrl());
        context.setVariable("adresseMedecin", medecin.getAdresseCabinet());
        String phonesString = medecin.getPhones() != null ? medecin.getPhones().stream().map(p -> formatPhoneNumber(p.getNumber())).collect(java.util.stream.Collectors.joining(" - ")) : "";
        context.setVariable("telephoneMedecin", phonesString);

        // 2. Générer le HTML
        String htmlContent = templateEngine.process("ordonnance", context);

        // 3. Convertir en PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            
            // Pour résoudre les chemins d'images "src='/images/...'", on donne la racine absolue static
            String baseUri = new ClassPathResource("static/").getURL().toExternalForm();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(os);
            builder.run();

            byte[] pdfBytes = os.toByteArray();

            // 4. Upload sur Cloudinary
            String filename = "ordonnance_" + appointment.getId() + "_" + UUID.randomUUID().toString();
            return cloudinaryService.uploadFile(pdfBytes, filename);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF de l'ordonnance", e);
        }
    }
    public String generateAndUploadDemandeAnalyse(Appointment appointment, DemandeAnalyseRequestDTO requestDTO) {
        Medecin medecin = appointment.getMedecin();
        Patient patient = appointment.getPatient();

        if (medecin == null || patient == null) {
            throw new RuntimeException("Données du médecin ou du patient manquantes pour ce rendez-vous");
        }

        // 1. Préparer le contexte Thymeleaf
        Context context = new Context();
        context.setVariable("nomMedecin", medecin.getNom() + " " + medecin.getPrenom());
        context.setVariable("specialiteMedecin", medecin.getSpecialite() != null ? medecin.getSpecialite() : "Médecin");
        context.setVariable("villeMedecin", medecin.getVille() != null ? medecin.getVille() : "");
        context.setVariable("dateAujourdhui", LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        context.setVariable("nomPatient", patient.getNom() + " " + patient.getPrenom());
        // On injecte nos analyses dans la variable "medicaments" pour que le template HTML s'affiche correctement
        context.setVariable("medicaments", requestDTO.getAnalyses());
        context.setVariable("signatureMedecin", medecin.getSignatureUrl());
        context.setVariable("cachetMedecin", medecin.getCachetUrl());
        context.setVariable("adresseMedecin", medecin.getAdresseCabinet());
        String phonesString2 = medecin.getPhones() != null ? medecin.getPhones().stream().map(p -> formatPhoneNumber(p.getNumber())).collect(java.util.stream.Collectors.joining(" - ")) : "";
        context.setVariable("telephoneMedecin", phonesString2);

        // 2. Générer le HTML en utilisant ordonnance.html
        String htmlContent = templateEngine.process("ordonnance", context);

        // 3. Convertir en PDF
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            
            String baseUri = new ClassPathResource("static/").getURL().toExternalForm();

            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            builder.withHtmlContent(htmlContent, baseUri);
            builder.toStream(os);
            builder.run();

            byte[] pdfBytes = os.toByteArray();

            // 4. Upload sur Cloudinary avec un nom approprié
            String filename = "demande_analyse_" + appointment.getId() + "_" + UUID.randomUUID().toString();
            return cloudinaryService.uploadFile(pdfBytes, filename);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF de la demande d'analyse", e);
        }
    }
}
