package com.sehati.auth.dto;

import java.util.List;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupLaboRequest {
    @NotBlank(message = "Le nom du laboratoire est obligatoire")
    @Size(min = 3, message = "La longueur du nom doit être supérieure ou égale à 3")
    private String nomLabo;

    private String adresseComplete;
    private String ville;
    private java.util.List<String> phones;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Veuillez saisir une adresse email valide.")
    private String email;

    @NotEmpty(message = "Au moins une analyse doit être ajoutée")
    private List<String> analyses;

    @NotBlank(message = "Le registre de commerce est obligatoire")
    private String registreCommerceUrl;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "Le mot de passe doit contenir au moins 8 caractères, une lettre majuscule, un caractère spécial et un chiffre.")
    private String password;

    @NotBlank(message = "La confirmation du mot de passe est obligatoire")
    private String confirmPassword;
    
    private Double latitude;
    private Double longitude;
}
