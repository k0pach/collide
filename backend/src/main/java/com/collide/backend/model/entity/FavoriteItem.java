package com.collide.backend.model.entity;

import com.collide.backend.model.id.FavoriteItemId;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "favorite_items")
public class FavoriteItem {
    @EmbeddedId private FavoriteItemId id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @MapsId("userId") @JoinColumn(name = "user_id") private AppUser user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @MapsId("itemId") @JoinColumn(name = "item_id") private Item item;
    @Column(name = "created_at", nullable = false) private OffsetDateTime createdAt;
    @PrePersist public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
    public FavoriteItemId getId() { return id; }
    public void setId(FavoriteItemId id) { this.id = id; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
