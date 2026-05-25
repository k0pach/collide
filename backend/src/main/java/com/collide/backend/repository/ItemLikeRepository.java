package com.collide.backend.repository;

import com.collide.backend.model.entity.ItemLike;
import com.collide.backend.model.id.ItemLikeId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemLikeRepository extends JpaRepository<ItemLike, ItemLikeId> {
    long countByIdItemId(UUID itemId);
    long countByIdUserId(UUID userId);
}
