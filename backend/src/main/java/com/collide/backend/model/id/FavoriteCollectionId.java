package com.collide.backend.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class FavoriteCollectionId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "collection_id")
    private UUID collectionId;

    public FavoriteCollectionId() {}
    public FavoriteCollectionId(UUID userId, UUID collectionId) { this.userId = userId; this.collectionId = collectionId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getCollectionId() { return collectionId; }
    public void setCollectionId(UUID collectionId) { this.collectionId = collectionId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof FavoriteCollectionId that)) return false; return Objects.equals(userId, that.userId) && Objects.equals(collectionId, that.collectionId); }
    @Override public int hashCode() { return Objects.hash(userId, collectionId); }
}
