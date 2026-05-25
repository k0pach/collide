package com.collide.backend.repository;

import com.collide.backend.model.entity.FavoriteCollection;
import com.collide.backend.model.id.FavoriteCollectionId;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteCollectionRepository extends JpaRepository<FavoriteCollection, FavoriteCollectionId> {
    List<FavoriteCollection> findByIdUserId(UUID userId);
}
