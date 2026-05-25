package com.collide.backend.model.entity;

import com.collide.backend.model.id.UserFollowId;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_follows")
public class UserFollow {
    @EmbeddedId
    private UserFollowId id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("followerId")
    @JoinColumn(name = "follower_id")
    private AppUser follower;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId("followingId")
    @JoinColumn(name = "following_id")
    private AppUser following;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() { if (createdAt == null) createdAt = OffsetDateTime.now(); }

    public UserFollowId getId() { return id; }
    public void setId(UserFollowId id) { this.id = id; }
    public AppUser getFollower() { return follower; }
    public void setFollower(AppUser follower) { this.follower = follower; }
    public AppUser getFollowing() { return following; }
    public void setFollowing(AppUser following) { this.following = following; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
