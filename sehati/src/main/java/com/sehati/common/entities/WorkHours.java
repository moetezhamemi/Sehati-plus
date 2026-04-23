package com.sehati.common.entities;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "work_hours")
public class WorkHours {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "debut", column = @Column(name = "lundi_debut")),
        @AttributeOverride(name = "fin", column = @Column(name = "lundi_fin")),
        @AttributeOverride(name = "ferme", column = @Column(name = "lundi_ferme"))
    })
    private DaySchedule lundi;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "debut", column = @Column(name = "mardi_debut")),
        @AttributeOverride(name = "fin", column = @Column(name = "mardi_fin")),
        @AttributeOverride(name = "ferme", column = @Column(name = "mardi_ferme"))
    })
    private DaySchedule mardi;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "debut", column = @Column(name = "mercredi_debut")),
        @AttributeOverride(name = "fin", column = @Column(name = "mercredi_fin")),
        @AttributeOverride(name = "ferme", column = @Column(name = "mercredi_ferme"))
    })
    private DaySchedule mercredi;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "debut", column = @Column(name = "jeudi_debut")),
        @AttributeOverride(name = "fin", column = @Column(name = "jeudi_fin")),
        @AttributeOverride(name = "ferme", column = @Column(name = "jeudi_ferme"))
    })
    private DaySchedule jeudi;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "debut", column = @Column(name = "vendredi_debut")),
        @AttributeOverride(name = "fin", column = @Column(name = "vendredi_fin")),
        @AttributeOverride(name = "ferme", column = @Column(name = "vendredi_ferme"))
    })
    private DaySchedule vendredi;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "debut", column = @Column(name = "samedi_debut")),
        @AttributeOverride(name = "fin", column = @Column(name = "samedi_fin")),
        @AttributeOverride(name = "ferme", column = @Column(name = "samedi_ferme"))
    })
    private DaySchedule samedi;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "debut", column = @Column(name = "dimanche_debut")),
        @AttributeOverride(name = "fin", column = @Column(name = "dimanche_fin")),
        @AttributeOverride(name = "ferme", column = @Column(name = "dimanche_ferme"))
    })
    private DaySchedule dimanche;
}
