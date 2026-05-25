package com.collide.backend.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ItemLikeId implements Serializable {
    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "user_id")
    private UUID userId;

    public ItemLikeId() {}
    public ItemLikeId(UUID itemId, UUID userId) { this.itemId = itemId; this.userId = userId; }
    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof ItemLikeId that)) return false; return Objects.equals(itemId, that.itemId) && Objects.equals(userId, that.userId); }
    @Override public int hashCode() { return Objects.hash(itemId, userId); }
}
