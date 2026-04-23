package com.sehati.medecin.entities;


import com.sehati.common.entities.WorkHours;
import com.sehati.auth.entities.User;
import jakarta.persistence.CascadeType;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import com.sehati.common.entities.PhoneNumber;
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
    @OneToMany(mappedBy = "medecin", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<PhoneNumber> phones = new java.util.ArrayList<>();
    private String diplomeUrl;
    private String photoProfilUrl;
    private String cachetUrl;
    private String signatureUrl;
    private Double latitude;
    private Double longitude;
    @Column(columnDefinition = "TEXT")
    private String biographie;
    @Column(name = "consultation_time", columnDefinition = "integer default 30")
    private Integer consultationTime = 30;
    
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "work_hours_id")
    private WorkHours workHours;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
}
