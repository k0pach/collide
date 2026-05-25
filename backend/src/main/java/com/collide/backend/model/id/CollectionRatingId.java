package com.collide.backend.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CollectionRatingId implements Serializable {
    @Column(name = "collection_id")
    private UUID collectionId;

    @Column(name = "user_id")
    private UUID userId;

    public CollectionRatingId() {}
    public CollectionRatingId(UUID collectionId, UUID userId) { this.collectionId = collectionId; this.userId = userId; }
    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof CollectionRatingId that)) return false; return Objects.equals(collectionId, that.collectionId) && Objects.equals(userId, that.userId); }
    @Override public int hashCode() { return Objects.hash(collectionId, userId); }
}
