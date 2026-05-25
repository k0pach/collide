package com.collide.backend.repository;

import com.collide.backend.model.entity.CollectionRating;
import com.collide.backend.model.id.CollectionRatingId;
import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CollectionRatingRepository extends JpaRepository<CollectionRating, CollectionRatingId> {
    @Query("select avg(r.rating) from CollectionRating r where r.collection.id = :collectionId")
    Optional<Double> averageForCollection(@Param("collectionId") UUID collectionId);

    @Query("select avg(r.rating) from CollectionRating r where r.collection.owner.id = :ownerId")
    Optional<Double> averageForUserCollections(@Param("ownerId") UUID ownerId);

    long countByIdCollectionId(UUID collectionId);
}
