package com.collide.backend.repository;

import com.collide.backend.model.entity.ItemComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemCommentRepository extends JpaRepository<ItemComment, UUID> {
    List<ItemComment> findByItemIdOrderByCreatedAtAsc(UUID itemId);
    long countByItemId(UUID itemId);
}
