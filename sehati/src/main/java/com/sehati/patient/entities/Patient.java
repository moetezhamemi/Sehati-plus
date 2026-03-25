package com.sehati.patient.entities;

import com.sehati.auth.entities.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "patients")
public class Patient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String prenom;
    private String telephone;
    private LocalDate dateNaissance;
    private String photoProfilUrl;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}