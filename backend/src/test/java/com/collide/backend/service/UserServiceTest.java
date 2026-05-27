package com.collide.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.collide.backend.dto.ProfileStatsDto;
import com.collide.backend.dto.UserDto;
import com.collide.backend.dto.request.UserUpdateRequest;
import com.collide.backend.exception.BadRequestException;
import com.collide.backend.exception.NotFoundException;
import com.collide.backend.model.entity.AppUser;
import com.collide.backend.model.entity.Item;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.model.id.UserFollowId;
import com.collide.backend.repository.CollectionRatingRepository;
import com.collide.backend.repository.CollectionRepository;
import com.collide.backend.repository.ItemLikeRepository;
import com.collide.backend.repository.ItemRepository;
import com.collide.backend.repository.UserFollowRepository;
import com.collide.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserFollowRepository followRepository;
    @Mock
    private CollectionRepository collectionRepository;
    @Mock
    private ItemRepository itemRepository;
    @Mock
    private ItemLikeRepository itemLikeRepository;
    @Mock
    private CollectionRatingRepository ratingRepository;
    @Mock
    private DtoMapper mapper;

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService(userRepository, followRepository, collectionRepository, itemRepository, itemLikeRepository, ratingRepository, mapper);
    }

    @Test
    void getUserUsesFollowFlagFromRepository() {
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        AppUser user = user("alice");
        UserDto dto = userDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(followRepository.existsById(new UserFollowId(currentUserId, userId))).thenReturn(true);
        when(mapper.user(user, true)).thenReturn(dto);

        assertThat(service.getUser(userId, currentUserId)).isSameAs(dto);
    }

    @Test
    void getUserWithoutCurrentUserMapsAsNotFollowing() {
        UUID userId = UUID.randomUUID();
        AppUser user = user("alice");
        UserDto dto = userDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(mapper.user(user, false)).thenReturn(dto);

        assertThat(service.getUser(userId, null)).isSameAs(dto);
    }

    @Test
    void getUserWithCurrentUserButWithoutFollowMapsAsNotFollowing() {
        UUID userId = UUID.randomUUID();
        UUID currentUserId = UUID.randomUUID();
        AppUser user = user("alice");
        UserDto dto = userDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(followRepository.existsById(new UserFollowId(currentUserId, userId))).thenReturn(false);
        when(mapper.user(user, false)).thenReturn(dto);

        assertThat(service.getUser(userId, currentUserId)).isSameAs(dto);
    }

    @Test
    void statsAggregatesProfileNumbers() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user("owner")));

        Item i1 = new Item();
        i1.setId(UUID.randomUUID());
        i1.setPriceAmount(new BigDecimal("10.00"));
        i1.setCollection(new com.collide.backend.model.entity.CollectionEntity());
        Item i2 = new Item();
        i2.setId(UUID.randomUUID());
        i2.setPriceAmount(new BigDecimal("20.50"));
        i2.setCollection(new com.collide.backend.model.entity.CollectionEntity());
        Item i3 = new Item();
        i3.setId(UUID.randomUUID());
        i3.setPriceAmount(new BigDecimal("100"));
        i3.setCollection(null);
        Item i4 = new Item();
        i4.setId(UUID.randomUUID());
        i4.setPriceAmount(null);
        i4.setCollection(new com.collide.backend.model.entity.CollectionEntity());

        when(collectionRepository.countByOwnerId(userId)).thenReturn(2L);
        when(itemRepository.countByOwnerId(userId)).thenReturn(3L);
        when(followRepository.countByIdFollowingId(userId)).thenReturn(5L);
        when(followRepository.countByIdFollowerId(userId)).thenReturn(7L);
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of(i1, i2, i3, i4));
        when(itemLikeRepository.countByIdItemId(i1.getId())).thenReturn(1L);
        when(itemLikeRepository.countByIdItemId(i2.getId())).thenReturn(2L);
        when(itemLikeRepository.countByIdItemId(i3.getId())).thenReturn(3L);
        when(itemLikeRepository.countByIdItemId(i4.getId())).thenReturn(4L);
        when(ratingRepository.averageForUserCollections(userId)).thenReturn(Optional.of(4.44));
        when(mapper.money(new BigDecimal("30.50"))).thenReturn("30.5 ₽");
        when(mapper.round(4.44)).thenReturn(4.4);

        ProfileStatsDto result = service.stats(userId);

        assertThat(result.collectionsCount()).isEqualTo(2);
        assertThat(result.itemsCount()).isEqualTo(3);
        assertThat(result.totalItemLikes()).isEqualTo(10);
        assertThat(result.followersCount()).isEqualTo(5);
        assertThat(result.followingCount()).isEqualTo(7);
        assertThat(result.totalCollectionsValue()).isEqualByComparingTo("30.50");
        assertThat(result.totalCollectionsValueLabel()).isEqualTo("30.5 ₽");
        assertThat(result.collectionAverageRating()).isEqualTo(4.4);
    }

    @Test
    void statsUsesZeroRatingFallback() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user("owner")));
        when(collectionRepository.countByOwnerId(userId)).thenReturn(0L);
        when(itemRepository.countByOwnerId(userId)).thenReturn(0L);
        when(followRepository.countByIdFollowingId(userId)).thenReturn(0L);
        when(followRepository.countByIdFollowerId(userId)).thenReturn(0L);
        when(itemRepository.findByOwnerId(userId)).thenReturn(List.of());
        when(ratingRepository.averageForUserCollections(userId)).thenReturn(Optional.empty());
        when(mapper.money(BigDecimal.ZERO)).thenReturn("0 ₽");
        when(mapper.round(0.0)).thenReturn(0.0);

        ProfileStatsDto result = service.stats(userId);

        assertThat(result.collectionAverageRating()).isZero();
        assertThat(result.totalCollectionsValue()).isEqualByComparingTo("0");
    }

    @Test
    void updateMeUpdatesOnlyAllowedFieldsAndTrimsValues() {
        UUID userId = UUID.randomUUID();
        AppUser user = user("owner");
        user.setDisplayName("Old Name");
        UserDto dto = userDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.user(any(AppUser.class), anyBoolean())).thenReturn(dto);

        UserUpdateRequest request = new UserUpdateRequest("   ", "  About me  ", " https://img ", " online ");
        UserDto result = service.updateMe(userId, request);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(userRepository).save(captor.capture());
        AppUser saved = captor.getValue();
        assertThat(saved.getDisplayName()).isEqualTo("Old Name");
        assertThat(saved.getBio()).isEqualTo("About me");
        assertThat(saved.getAvatarUrl()).isEqualTo("https://img");
        assertThat(saved.getStatusMessage()).isEqualTo("online");
        assertThat(saved.getLastSeenAt()).isNotNull();
        assertThat(result).isSameAs(dto);
    }

    @Test
    void updateMeUpdatesDisplayNameAndSkipsNullOptionalFields() {
        UUID userId = UUID.randomUUID();
        AppUser user = user("owner");
        user.setDisplayName("Old Name");
        user.setBio("Old Bio");
        user.setAvatarUrl("old-avatar");
        user.setStatusMessage("old-status");
        UserDto dto = userDto();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.user(any(AppUser.class), anyBoolean())).thenReturn(dto);

        UserUpdateRequest request = new UserUpdateRequest("  New Name  ", null, null, null);
        service.updateMe(userId, request);

        assertThat(user.getDisplayName()).isEqualTo("New Name");
        assertThat(user.getBio()).isEqualTo("Old Bio");
        assertThat(user.getAvatarUrl()).isEqualTo("old-avatar");
        assertThat(user.getStatusMessage()).isEqualTo("old-status");
    }

    @Test
    void updateMeKeepsDisplayNameWhenNull() {
        UUID userId = UUID.randomUUID();
        AppUser user = user("owner");
        user.setDisplayName("Same Name");

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.user(any(AppUser.class), anyBoolean())).thenReturn(userDto());

        service.updateMe(userId, new UserUpdateRequest(null, "bio", null, null));

        assertThat(user.getDisplayName()).isEqualTo("Same Name");
        assertThat(user.getBio()).isEqualTo("bio");
    }

    @Test
    void followThrowsForSelfFollow() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> service.follow(id, id))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Нельзя подписаться на самого себя");
    }

    @Test
    void followCreatesNewRelationWhenNotExists() {
        UUID currentUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        AppUser current = user("current");
        AppUser target = user("target");
        UserDto dto = userDto();

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(current));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(followRepository.existsById(new UserFollowId(currentUserId, targetUserId))).thenReturn(false);
        when(mapper.user(target, true)).thenReturn(dto);

        assertThat(service.follow(currentUserId, targetUserId)).isSameAs(dto);
        verify(followRepository).save(any(com.collide.backend.model.entity.UserFollow.class));
    }

    @Test
    void followDoesNotCreateRelationWhenAlreadyExists() {
        UUID currentUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        AppUser current = user("current");
        AppUser target = user("target");
        UserDto dto = userDto();

        when(userRepository.findById(currentUserId)).thenReturn(Optional.of(current));
        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(followRepository.existsById(new UserFollowId(currentUserId, targetUserId))).thenReturn(true);
        when(mapper.user(target, true)).thenReturn(dto);

        assertThat(service.follow(currentUserId, targetUserId)).isSameAs(dto);
        verify(followRepository, never()).save(any());
    }

    @Test
    void unfollowDeletesAndReturnsMappedUser() {
        UUID currentUserId = UUID.randomUUID();
        UUID targetUserId = UUID.randomUUID();
        AppUser target = user("target");
        UserDto dto = userDto();

        when(userRepository.findById(targetUserId)).thenReturn(Optional.of(target));
        when(mapper.user(target, false)).thenReturn(dto);

        assertThat(service.unfollow(currentUserId, targetUserId)).isSameAs(dto);
        verify(followRepository).deleteById(new UserFollowId(currentUserId, targetUserId));
    }

    @Test
    void findThrowsWhenUserMissing() {
        UUID id = UUID.randomUUID();
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.find(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Пользователь не найден");
    }

    private AppUser user(String username) {
        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setUsername(username);
        user.setDisplayName(username);
        user.setEmail(username + "@mail.test");
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        return user;
    }

    private UserDto userDto() {
        return new UserDto(UUID.randomUUID(), "user", "@user", "User", "User", "", null, "orange", "Online", false);
    }
}
