-- 1. Список таблиц и представлений в схеме public
SELECT table_name, table_type
FROM information_schema.tables
WHERE table_schema = 'public'
ORDER BY table_type, table_name;

-- 2. Количество записей в основных таблицах
SELECT 'app_users' AS table_name, COUNT(*) FROM app_users
UNION ALL SELECT 'categories', COUNT(*) FROM categories
UNION ALL SELECT 'collections', COUNT(*) FROM collections
UNION ALL SELECT 'items', COUNT(*) FROM items
UNION ALL SELECT 'item_likes', COUNT(*) FROM item_likes
UNION ALL SELECT 'collection_ratings', COUNT(*) FROM collection_ratings
UNION ALL SELECT 'favorite_items', COUNT(*) FROM favorite_items
UNION ALL SELECT 'favorite_collections', COUNT(*) FROM favorite_collections
UNION ALL SELECT 'item_comments', COUNT(*) FROM item_comments
UNION ALL SELECT 'collection_comments', COUNT(*) FROM collection_comments
UNION ALL SELECT 'user_follows', COUNT(*) FROM user_follows
UNION ALL SELECT 'chats', COUNT(*) FROM chats
UNION ALL SELECT 'messages', COUNT(*) FROM messages
ORDER BY table_name;

-- 3. Справочник категорий
SELECT id, slug, title, sort_order
FROM categories
ORDER BY sort_order;

-- 4. Пользователи без вывода password_hash
SELECT id, email, username, display_name, role, is_enabled, created_at
FROM app_users
ORDER BY created_at DESC;

-- 5. Коллекции с владельцем и категорией
SELECT
    c.id,
    c.title,
    u.username AS owner_username,
    cat.title AS category_title,
    c.visibility,
    c.created_at
FROM collections c
JOIN app_users u ON u.id = c.owner_id
LEFT JOIN categories cat ON cat.id = c.category_id
ORDER BY c.created_at DESC;

-- 6. Предметы с владельцем, коллекцией и категорией
SELECT
    i.id,
    i.title,
    u.username AS owner_username,
    c.title AS collection_title,
    cat.title AS category_title,
    i.status,
    i.price_amount,
    i.currency,
    i.created_at
FROM items i
JOIN app_users u ON u.id = i.owner_id
LEFT JOIN collections c ON c.id = i.collection_id
LEFT JOIN categories cat ON cat.id = i.category_id
ORDER BY i.created_at DESC;

-- 7. Предметы без коллекции
SELECT id, title, owner_id, category_id, price_amount
FROM items
WHERE collection_id IS NULL
ORDER BY created_at DESC;

-- 8. Статистика коллекций
SELECT *
FROM collection_stats
ORDER BY total_price_amount DESC;

-- 9. Статистика профилей
SELECT
    u.username,
    s.collection_count,
    s.item_count,
    s.item_likes_count,
    s.followers_count,
    s.total_collections_price,
    s.avg_collection_rating
FROM user_profile_stats s
JOIN app_users u ON u.id = s.user_id
ORDER BY u.username;

-- 10. Избранное предметов
SELECT
    u.username,
    i.title AS item_title,
    fi.created_at
FROM favorite_items fi
JOIN app_users u ON u.id = fi.user_id
JOIN items i ON i.id = fi.item_id
ORDER BY fi.created_at DESC;

-- 11. Избранное коллекций
SELECT
    u.username,
    c.title AS collection_title,
    fc.created_at
FROM favorite_collections fc
JOIN app_users u ON u.id = fc.user_id
JOIN collections c ON c.id = fc.collection_id
ORDER BY fc.created_at DESC;

-- 12. Комментарии к предметам
SELECT
    i.title AS item_title,
    u.username AS author_username,
    ic.body,
    ic.created_at
FROM item_comments ic
JOIN items i ON i.id = ic.item_id
JOIN app_users u ON u.id = ic.author_id
ORDER BY ic.created_at DESC;

-- 13. Комментарии к коллекциям
SELECT
    c.title AS collection_title,
    u.username AS author_username,
    cc.body,
    cc.created_at
FROM collection_comments cc
JOIN collections c ON c.id = cc.collection_id
JOIN app_users u ON u.id = cc.author_id
ORDER BY cc.created_at DESC;

-- 14. Личные чаты
SELECT
    ch.id,
    u1.username AS first_user,
    u2.username AS second_user,
    ch.created_at,
    ch.updated_at
FROM chats ch
JOIN app_users u1 ON u1.id = ch.first_user_id
JOIN app_users u2 ON u2.id = ch.second_user_id
ORDER BY ch.updated_at DESC;

-- 15. Последние сообщения
SELECT
    m.id,
    ch.id AS chat_id,
    u.username AS sender_username,
    m.body,
    m.is_deleted,
    m.created_at
FROM messages m
JOIN chats ch ON ch.id = m.chat_id
JOIN app_users u ON u.id = m.sender_id
ORDER BY m.created_at DESC
LIMIT 50;

-- 16. Проверка дублей личных чатов между одной парой пользователей
SELECT
    LEAST(first_user_id, second_user_id) AS user_a,
    GREATEST(first_user_id, second_user_id) AS user_b,
    COUNT(*) AS chats_count
FROM chats
GROUP BY user_a, user_b
HAVING COUNT(*) > 1;
