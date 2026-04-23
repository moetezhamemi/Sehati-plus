package com.sehati.laboratoire.entities;

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
import jakarta.persistence.ElementCollection;
import com.sehati.common.entities.PhoneNumber;
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
    @OneToMany(mappedBy = "laboratoire", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PhoneNumber> phones = new java.util.ArrayList<>();
    private String responsable;    
    @ElementCollection
    private List<String> analyses;

    private Double latitude;
    private Double longitude;
    private String registreCommerceUrl;
    private String photoProfilUrl;
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "work_hours_id")
    private WorkHours workHours;
    
    private Integer consultationTime;
    private Integer capaciteParCreneau; // Number of patients that can be handled simultaneously in one time slot

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

}
