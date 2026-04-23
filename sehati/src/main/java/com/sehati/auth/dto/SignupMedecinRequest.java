package com.sehati.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupMedecinRequest {
    @NotBlank(message = "Le nom est obligatoire")
    @Size(min = 3, message = "La longueur du nom doit être d'au moins 3 caractères")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(min = 3, message = "La longueur du prénom doit être d'au moins 3 caractères")
    private String prenom;

    @NotBlank(message = "La spécialité est obligatoire")
    private String specialite;

    private String adresseCabinet;
    private String ville;
    private java.util.List<String> phones;
    private String diplomeUrl;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email doit être valide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "Le mot de passe doit contenir au moins 8 caractères, une lettre majuscule, un caractère spécial et un chiffre.")
    private String password;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
    
    // Optional coordinates if map is used
    private Double latitude;
    private Double longitude;
}
