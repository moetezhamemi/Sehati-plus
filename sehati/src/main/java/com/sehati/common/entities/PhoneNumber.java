package com.sehati.common.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sehati.laboratoire.entities.Laboratoire;
import com.sehati.medecin.entities.Medecin;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "phone_numbers")
public class PhoneNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medecin_id")
    @JsonIgnore
    private Medecin medecin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "laboratoire_id")
    @JsonIgnore
    private Laboratoire laboratoire;

    public PhoneNumber(String number) {
        this.number = number;
    }
}
