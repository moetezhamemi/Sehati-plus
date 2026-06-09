package com.sehati.ai.repositories;

import com.sehati.ai.entities.AiMessage;
import com.sehati.ai.entities.AiConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiMessageRepository extends JpaRepository<AiMessage, Long> {
    List<AiMessage> findByConversationOrderByTimestampAsc(AiConversation conversation);
}
