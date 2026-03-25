package com.sehati.medecin.entities;


import com.sehati.auth.entities.User;
import com.sehati.secretaire.entities.Secretaire;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;


@Data @NoArgsConstructor @AllArgsConstructor
@Entity
@Table(name = "medecins")
public class Medecin {
    @Id 
	@GeneratedValue (strategy=GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    private String specialite;
    private String adresseCabinet;
    private String ville;
    private String telephone;
    private String diplomeUrl;
    private String photoProfilUrl;
    private Double latitude;
    private Double longitude;
    @OneToOne(mappedBy = "medecin")
    private Secretaire secretaire;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    
}
