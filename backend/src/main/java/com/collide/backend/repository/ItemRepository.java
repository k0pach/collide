package com.collide.backend.repository;

import com.collide.backend.model.entity.Item;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, UUID> {
    List<Item> findByOwnerId(UUID ownerId);
    List<Item> findByCollectionId(UUID collectionId);
    long countByOwnerId(UUID ownerId);
}
