package com.collide.backend.repository;

import com.collide.backend.model.entity.CollectionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<CollectionEntity, UUID> {
    List<CollectionEntity> findByOwnerId(UUID ownerId);
    long countByOwnerId(UUID ownerId);
}
