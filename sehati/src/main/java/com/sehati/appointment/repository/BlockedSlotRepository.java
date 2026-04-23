package com.sehati.appointment.repository;

import com.sehati.appointment.entities.BlockedSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BlockedSlotRepository extends JpaRepository<BlockedSlot, Long> {
    List<BlockedSlot> findByOwnerIdAndOwnerTypeAndDate(Long ownerId, String ownerType, LocalDate date);
    List<BlockedSlot> findByOwnerIdAndOwnerTypeAndDateBetween(Long ownerId, String ownerType, LocalDate start, LocalDate end);
}
