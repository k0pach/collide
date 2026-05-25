package com.collide.backend.model.entity;

import com.collide.backend.model.id.ItemLikeId;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "item_likes")
public class ItemLike {
    @EmbeddedId
    private ItemLikeId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("itemId")
    @JoinColumn(name = "item_id")
    private Item item;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;
    @PrePersist public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }
    public ItemLikeId getId() { return id; }
    public void setId(ItemLikeId id) { this.id = id; }
    public Item getItem() { return item; }
    public void setItem(Item item) { this.item = item; }
    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
