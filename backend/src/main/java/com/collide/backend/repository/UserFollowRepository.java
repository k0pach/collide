package com.collide.backend.repository;

import com.collide.backend.model.entity.UserFollow;
import com.collide.backend.model.id.UserFollowId;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserFollowRepository extends JpaRepository<UserFollow, UserFollowId> {
    long countByIdFollowingId(UUID followingId);
    long countByIdFollowerId(UUID followerId);
}
