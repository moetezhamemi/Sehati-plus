package com.sehati.secretaire.entities;

import com.sehati.auth.entities.User;
import com.sehati.medecin.entities.Medecin;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "secretaires")
public class Secretaire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private String prenom;

    @OneToOne
    @JoinColumn(name = "medecin_id") 
    private Medecin medecin;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
