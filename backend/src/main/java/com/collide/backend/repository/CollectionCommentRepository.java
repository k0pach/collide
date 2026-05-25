package com.collide.backend.repository;

import com.collide.backend.model.entity.CollectionComment;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionCommentRepository extends JpaRepository<CollectionComment, UUID> {
    List<CollectionComment> findByCollectionIdOrderByCreatedAtAsc(UUID collectionId);
    long countByCollectionId(UUID collectionId);
}
