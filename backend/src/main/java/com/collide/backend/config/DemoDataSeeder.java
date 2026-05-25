package com.collide.backend.config;

import com.collide.backend.model.entity.*;
import com.collide.backend.model.enums.ItemStatus;
import com.collide.backend.model.enums.UserRole;
import com.collide.backend.model.enums.Visibility;
import com.collide.backend.model.id.CollectionRatingId;
import com.collide.backend.model.id.ItemLikeId;
import com.collide.backend.model.id.UserFollowId;
import com.collide.backend.repository.*;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DemoDataSeeder implements ApplicationRunner {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final CollectionRepository collectionRepository;
    private final ItemRepository itemRepository;
    private final ItemLikeRepository itemLikeRepository;
    private final CollectionRatingRepository ratingRepository;
    private final UserFollowRepository followRepository;
    private final ChatRepository chatRepository;
    private final MessageRepository messageRepository;
    private final PasswordEncoder passwordEncoder;

    public DemoDataSeeder(UserRepository userRepository, CategoryRepository categoryRepository, CollectionRepository collectionRepository, ItemRepository itemRepository, ItemLikeRepository itemLikeRepository, CollectionRatingRepository ratingRepository, UserFollowRepository followRepository, ChatRepository chatRepository, MessageRepository messageRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.collectionRepository = collectionRepository;
        this.itemRepository = itemRepository;
        this.itemLikeRepository = itemLikeRepository;
        this.ratingRepository = ratingRepository;
        this.followRepository = followRepository;
        this.chatRepository = chatRepository;
        this.messageRepository = messageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedCategories();
        fixOldDemoPasswords();
        if (userRepository.count() > 0) return;
        seedDemoUsersAndContent();
    }

    private void seedCategories() {
        Map<String, String> categories = new LinkedHashMap<>();
        categories.put("figures", "Фигурки");
        categories.put("coins", "Монеты");
        categories.put("records", "Пластинки");
        categories.put("books", "Книги");
        categories.put("watches", "Часы");
        categories.put("retro_tech", "Ретро-техника");
        categories.put("table_games", "Настольные игры");
        categories.put("minerals", "Минералы");
        int order = 10;
        for (Map.Entry<String, String> entry : categories.entrySet()) {
            Category category = categoryRepository.findBySlug(entry.getKey()).orElseGet(Category::new);
            category.setSlug(entry.getKey());
            category.setTitle(entry.getValue());
            category.setSortOrder(order);
            categoryRepository.save(category);
            order += 10;
        }
    }

    private void fixOldDemoPasswords() {
        userRepository.findAll().stream().filter(user -> "demo-password-hash".equals(user.getPasswordHash())).forEach(user -> user.setPasswordHash(passwordEncoder.encode("password")));
    }

    private void seedDemoUsersAndContent() {
        AppUser jesse = user("jesseyo@example.com", "jesseyo", "JesseYo", "Коллекционирую фигурки, комиксы и редкие предметы из поп-культуры.");
        AppUser walter = user("heisenberg@example.com", "heisenberg", "Heisenberg", "Собираю минералы, ретро-технику и уникальные предметы.");
        AppUser mike = user("mike@example.com", "mike", "Mike Waltuh", "Люблю порядок в коллекциях и хорошие сделки.");
        userRepository.save(jesse);
        userRepository.save(walter);
        userRepository.save(mike);

        Category figures = categoryRepository.findBySlug("figures").orElseThrow();
        Category records = categoryRepository.findBySlug("records").orElseThrow();
        Category books = categoryRepository.findBySlug("books").orElseThrow();
        Category minerals = categoryRepository.findBySlug("minerals").orElseThrow();

        CollectionEntity pop = collection(jesse, figures, "Pop Culture Shelf", "Фигурки и коллекционные издания по любимым фильмам и сериалам.");
        CollectionEntity vinyl = collection(jesse, records, "Винил 80-х", "Пластинки с классическим звучанием и тёплой эстетикой.");
        CollectionEntity mineralsCollection = collection(walter, minerals, "Blue Minerals", "Минералы, образцы пород и редкие кристаллы.");
        collectionRepository.save(pop);
        collectionRepository.save(vinyl);
        collectionRepository.save(mineralsCollection);

        Item i1 = item(jesse, pop, figures, "Фигурка героя", "Редкая фигурка в отличном состоянии", "Оригинальная фигурка с коробкой и подставкой. Подходит для обмена или витрины.", "4500", ItemStatus.IN_COLLECTION);
        Item i2 = item(jesse, pop, books, "Комикс выпуск #12", "Лимитированный выпуск комикса", "Комикс с альтернативной обложкой, состояние близкое к новому.", "2900", ItemStatus.FOR_EXCHANGE);
        Item i3 = item(jesse, vinyl, records, "Synth Wave LP", "Пластинка с тёплым звучанием", "Виниловая пластинка, проверена на проигрывателе, без заметных дефектов.", "3700", ItemStatus.FOR_SALE);
        Item i4 = item(walter, mineralsCollection, minerals, "Азурит", "Минерал глубокого синего цвета", "Образец минерала с насыщенной окраской, хранится в защитном боксе.", "5200", ItemStatus.IN_COLLECTION);
        itemRepository.save(i1);
        itemRepository.save(i2);
        itemRepository.save(i3);
        itemRepository.save(i4);

        like(i1, walter);
        like(i1, mike);
        like(i2, walter);
        like(i3, mike);
        rate(pop, walter, (short) 5);
        rate(pop, mike, (short) 4);
        rate(vinyl, mike, (short) 5);
        follow(walter, jesse);
        follow(mike, jesse);

        Chat chat = new Chat();
        chat.setFirstUser(jesse);
        chat.setSecondUser(walter);
        chatRepository.save(chat);
        Message m = new Message();
        m.setChat(chat);
        m.setSender(walter);
        m.setBody("Привет! Интересует обмен по фигурке.");
        messageRepository.save(m);
    }

    private AppUser user(String email, String username, String name, String bio) {
        AppUser user = new AppUser();
        user.setEmail(email);
        user.setUsername(username);
        user.setDisplayName(name);
        user.setBio(bio);
        user.setPasswordHash(passwordEncoder.encode("password"));
        user.setRole(UserRole.USER);
        user.setEnabled(true);
        user.setStatusMessage("Заходил недавно");
        return user;
    }

    private CollectionEntity collection(AppUser owner, Category category, String title, String description) {
        CollectionEntity collection = new CollectionEntity();
        collection.setOwner(owner);
        collection.setCategory(category);
        collection.setTitle(title);
        collection.setDescription(description);
        collection.setVisibility(Visibility.PUBLIC);
        return collection;
    }

    private Item item(AppUser owner, CollectionEntity collection, Category category, String title, String shortDescription, String fullDescription, String price, ItemStatus status) {
        Item item = new Item();
        item.setOwner(owner);
        item.setCollection(collection);
        item.setCategory(category);
        item.setTitle(title);
        item.setShortDescription(shortDescription.length() > 60 ? shortDescription.substring(0, 60) : shortDescription);
        item.setFullDescription(fullDescription);
        item.setPriceAmount(new BigDecimal(price));
        item.setCurrency("RUB");
        item.setStatus(status);
        item.setVisibility(Visibility.PUBLIC);
        return item;
    }

    private void like(Item item, AppUser user) {
        ItemLike like = new ItemLike();
        like.setId(new ItemLikeId(item.getId(), user.getId()));
        like.setItem(item);
        like.setUser(user);
        itemLikeRepository.save(like);
    }

    private void rate(CollectionEntity collection, AppUser user, short value) {
        CollectionRating rating = new CollectionRating();
        rating.setId(new CollectionRatingId(collection.getId(), user.getId()));
        rating.setCollection(collection);
        rating.setUser(user);
        rating.setRating(value);
        ratingRepository.save(rating);
    }

    private void follow(AppUser follower, AppUser following) {
        UserFollow follow = new UserFollow();
        follow.setId(new UserFollowId(follower.getId(), following.getId()));
        follow.setFollower(follower);
        follow.setFollowing(following);
        followRepository.save(follow);
    }
}
