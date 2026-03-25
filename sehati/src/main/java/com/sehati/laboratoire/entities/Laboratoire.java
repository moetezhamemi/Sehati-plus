package com.sehati.laboratoire.entities;

import com.sehati.auth.entities.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.ElementCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "laboratoires")
public class Laboratoire {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nomLabo;
    private String adresseComplete;
    private String ville;
    private String telephone;
    private String email;
    
    @ElementCollection
    private List<String> analyses;

    private Double latitude;
    private Double longitude;
    private String registreCommerceUrl;
    private String photoProfilUrl;
    private String certificationUrl;
    private String autorisationUrl;
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
