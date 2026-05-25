package com.collide.backend.model.id;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class FavoriteItemId implements Serializable {
    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "item_id")
    private UUID itemId;

    public FavoriteItemId() {}
    public FavoriteItemId(UUID userId, UUID itemId) { this.userId = userId; this.itemId = itemId; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }
    @Override public boolean equals(Object o) { if (this == o) return true; if (!(o instanceof FavoriteItemId that)) return false; return Objects.equals(userId, that.userId) && Objects.equals(itemId, that.itemId); }
    @Override public int hashCode() { return Objects.hash(userId, itemId); }
}
