package com.collide.backend.repository;

import com.collide.backend.model.entity.FavoriteItem;
import com.collide.backend.model.id.FavoriteItemId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteItemRepository extends JpaRepository<FavoriteItem, FavoriteItemId> {
    List<FavoriteItem> findByIdUserId(UUID userId);
}
