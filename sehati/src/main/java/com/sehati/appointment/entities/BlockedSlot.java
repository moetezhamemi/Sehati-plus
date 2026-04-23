package com.sehati.appointment.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "blocked_slots")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The ID of the owner blocking the slot (e.g., Medecin ID or Laboratoire ID)
    private Long ownerId;

    // Type of owner: "MEDECIN" or "LABORATOIRE"
    private String ownerType;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
}
