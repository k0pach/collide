package com.collide.backend.model.entity;

import com.collide.backend.model.id.FavoriteCollectionId;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "favorite_collections")
public class FavoriteCollection {
    @EmbeddedId private FavoriteCollectionId id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @MapsId("userId") @JoinColumn(name = "user_id") private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @MapsId("collectionId") @JoinColumn(name = "collection_id") private CollectionEntity collection;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @PrePersist public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
    public FavoriteCollectionId getId() { return id; }
    public void setId(FavoriteCollectionId id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public CollectionEntity getCollection() { return collection; }
    public void setCollection(CollectionEntity collection) { this.collection = collection; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
