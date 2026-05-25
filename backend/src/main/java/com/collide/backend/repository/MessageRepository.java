package com.collide.backend.repository;

import com.collide.backend.model.entity.Message;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByChatIdAndDeletedFalseOrderByCreatedAtAsc(UUID chatId);
    Optional<Message> findTopByChatIdAndDeletedFalseOrderByCreatedAtDesc(UUID chatId);
    long countByChat_IdAndDeletedFalseAndSender_IdNot(UUID chatId, UUID senderId);
    long countByChat_IdAndDeletedFalseAndCreatedAtAfterAndSender_IdNot(UUID chatId, OffsetDateTime after, UUID senderId);
}
