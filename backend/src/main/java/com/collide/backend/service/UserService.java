package com.collide.backend.service;

import com.collide.backend.dto.ProfileStatsDto;
import com.collide.backend.dto.UserDto;
import com.collide.backend.dto.request.UserUpdateRequest;
import com.collide.backend.exception.BadRequestException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.entity.Item;
import com.collide.backend.model.entity.UserFollow;
import com.collide.backend.model.id.UserFollowId;
import com.collide.backend.repository.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserFollowRepository followRepository;
    private final CollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final ItemLikeRepository itemLikeRepository;
    private final CollectionRatingRepository ratingRepository;
    private final DtoMapper mapper;

    public UserService(UserRepository userRepository, UserFollowRepository followRepository, CollectionRepository collectionRepository, ItemRepository itemRepository, ItemLikeRepository itemLikeRepository, CollectionRatingRepository ratingRepository, DtoMapper mapper) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.itemLikeRepository = itemLikeRepository;
        this.ratingRepository = ratingRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public UserDto getUser(UUID userId, UUID currentUserId) {
        AppUser user = find(userId);
        boolean following = currentUserId != null && followRepository.existsById(new UserFollowId(currentUserId, userId));
        return mapper.user(user, following);
    }

    @Transactional(readOnly = true)
    public ProfileStatsDto stats(UUID userId) {
        find(userId);
        long collectionsCount = collectionRepository.countByOwnerId(userId);
        long itemsCount = itemRepository.countByOwnerId(userId);
        long followers = followRepository.countByIdFollowingId(userId);
        long following = followRepository.countByIdFollowerId(userId);
        long itemLikes = itemRepository.findByOwnerId(userId).stream().mapToLong(item -> itemLikeRepository.countByIdItemId(item.getId())).sum();
        BigDecimal totalValue = itemRepository.findByOwnerId(userId).stream().filter(item -> item.getCollection() != null).map(Item::getPriceAmount).filter(v -> v != null).reduce(BigDecimal.ZERO, BigDecimal::add);
        double avgRating = ratingRepository.averageForUserCollections(userId).orElse(0.0);
        return new ProfileStatsDto(collectionsCount, itemsCount, itemLikes, followers, following, totalValue, mapper.money(totalValue), mapper.round(avgRating));
    }

    @Transactional
    public UserDto updateMe(UUID currentUserId, UserUpdateRequest request) {
        AppUser user = find(currentUserId);
        if (request.displayName() != null && !request.displayName().isBlank())
            user.setDisplayName(request.displayName().trim());
        if (request.bio() != null) user.setBio(request.bio().trim());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl().trim());
        if (request.statusMessage() != null) user.setStatusMessage(request.statusMessage().trim());
        user.setLastSeenAt(OffsetDateTime.now());
        return mapper.user(userRepository.save(user), false);
    }

    @Transactional
    public UserDto follow(UUID currentUserId, UUID targetUserId) {
        if (currentUserId.equals(targetUserId)) throw new BadRequestException("Нельзя подписаться на самого себя");
        AppUser current = find(currentUserId);
        AppUser target = find(targetUserId);
        UserFollowId id = new UserFollowId(currentUserId, targetUserId);
        if (!followRepository.existsById(id)) {
            UserFollow follow = new UserFollow();
            follow.setId(id);
            follow.setFollower(current);
            follow.setFollowing(target);
            followRepository.save(follow);
        }
        return mapper.user(target, true);
    }

    @Transactional
    public UserDto unfollow(UUID currentUserId, UUID targetUserId) {
        followRepository.deleteById(new UserFollowId(currentUserId, targetUserId));
        return mapper.user(find(targetUserId), false);
    }

    public AppUser find(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}
