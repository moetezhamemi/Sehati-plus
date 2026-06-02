package com.sehati.support.repository;

import com.sehati.support.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.sehati.support.entity.MessageStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    @Query("SELECT m FROM ContactMessage m WHERE " +
           "(:status IS NULL OR m.status = :status) AND " +
           "(:subject IS NULL OR LOWER(m.subject) LIKE LOWER(CONCAT('%', :subject, '%')))")
    Page<ContactMessage> findByFilters(@Param("status") MessageStatus status, 
                                       @Param("subject") String subject, 
                                       Pageable pageable);
}
