BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================================================
-- Users
-- =========================================================

CREATE TABLE IF NOT EXISTS app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    email VARCHAR(254) NOT NULL,
    username VARCHAR(50) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,

    display_name VARCHAR(80) NOT NULL,
    bio VARCHAR(500),
    avatar_url TEXT,
    status_message VARCHAR(120) DEFAULT 'Заходил недавно',

    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    is_enabled BOOLEAN NOT NULL DEFAULT TRUE,

    last_seen_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_app_users_role
        CHECK (role IN ('USER', 'ADMIN')),

    CONSTRAINT chk_app_users_email_not_blank
        CHECK (length(trim(email)) > 0),

    CONSTRAINT chk_app_users_username_not_blank
        CHECK (length(trim(username)) > 0),

    CONSTRAINT chk_app_users_display_name_not_blank
        CHECK (length(trim(display_name)) > 0)
);

CREATE UNIQUE INDEX IF NOT EXISTS ux_app_users_email_lower
    ON app_users (lower(email));

CREATE UNIQUE INDEX IF NOT EXISTS ux_app_users_username_lower
    ON app_users (lower(username));


-- =========================================================
-- User subscriptions
-- =========================================================

CREATE TABLE IF NOT EXISTS user_follows (
    follower_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    following_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (follower_id, following_id),

    CONSTRAINT chk_user_follows_not_self
        CHECK (follower_id <> following_id)
);

CREATE INDEX IF NOT EXISTS idx_user_follows_following_id
    ON user_follows (following_id);


-- =========================================================
-- Categories
-- =========================================================

CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    slug VARCHAR(60) NOT NULL UNIQUE,
    title VARCHAR(100) NOT NULL,
    sort_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_categories_slug_not_blank
        CHECK (length(trim(slug)) > 0),

    CONSTRAINT chk_categories_title_not_blank
        CHECK (length(trim(title)) > 0)
);


-- =========================================================
-- Collections
-- =========================================================

CREATE TABLE IF NOT EXISTS collections (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,

    title VARCHAR(120) NOT NULL,
    description TEXT,
    cover_image_url TEXT,

    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_collections_title_not_blank
        CHECK (length(trim(title)) > 0),

    CONSTRAINT chk_collections_visibility
        CHECK (visibility IN ('PUBLIC', 'PRIVATE'))
);

CREATE INDEX IF NOT EXISTS idx_collections_owner_id
    ON collections (owner_id);

CREATE INDEX IF NOT EXISTS idx_collections_category_id
    ON collections (category_id);

CREATE INDEX IF NOT EXISTS idx_collections_created_at
    ON collections (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_collections_title_lower
    ON collections (lower(title));


-- =========================================================
-- Items
-- =========================================================

CREATE TABLE IF NOT EXISTS items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    owner_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    collection_id UUID REFERENCES collections(id) ON DELETE SET NULL,
    category_id UUID REFERENCES categories(id) ON DELETE SET NULL,

    title VARCHAR(120) NOT NULL,
    short_description VARCHAR(60),
    full_description TEXT,

    image_url TEXT,

    price_amount NUMERIC(12, 2),
    currency CHAR(3) NOT NULL DEFAULT 'RUB',

    status VARCHAR(30) NOT NULL DEFAULT 'IN_COLLECTION',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_items_title_not_blank
        CHECK (length(trim(title)) > 0),

    CONSTRAINT chk_items_price_non_negative
        CHECK (price_amount IS NULL OR price_amount >= 0),

    CONSTRAINT chk_items_status
        CHECK (status IN ('IN_COLLECTION', 'FOR_SALE', 'FOR_EXCHANGE', 'ARCHIVED')),

    CONSTRAINT chk_items_visibility
        CHECK (visibility IN ('PUBLIC', 'PRIVATE')),

    CONSTRAINT chk_items_currency
        CHECK (currency IN ('RUB', 'USD', 'EUR'))
);

CREATE INDEX IF NOT EXISTS idx_items_owner_id
    ON items (owner_id);

CREATE INDEX IF NOT EXISTS idx_items_collection_id
    ON items (collection_id);

CREATE INDEX IF NOT EXISTS idx_items_category_id
    ON items (category_id);

CREATE INDEX IF NOT EXISTS idx_items_status
    ON items (status);

CREATE INDEX IF NOT EXISTS idx_items_created_at
    ON items (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_items_price_amount
    ON items (price_amount);

CREATE INDEX IF NOT EXISTS idx_items_title_lower
    ON items (lower(title));


-- =========================================================
-- Item likes
-- =========================================================

CREATE TABLE IF NOT EXISTS item_likes (
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (item_id, user_id)
);

CREATE INDEX IF NOT EXISTS idx_item_likes_user_id
    ON item_likes (user_id);


-- =========================================================
-- Collection ratings
-- =========================================================

CREATE TABLE IF NOT EXISTS collection_ratings (
    collection_id UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,

    rating SMALLINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (collection_id, user_id),

    CONSTRAINT chk_collection_ratings_rating
        CHECK (rating BETWEEN 1 AND 5)
);

CREATE INDEX IF NOT EXISTS idx_collection_ratings_user_id
    ON collection_ratings (user_id);


-- =========================================================
-- Favorites
-- =========================================================

CREATE TABLE IF NOT EXISTS favorite_items (
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, item_id)
);

CREATE INDEX IF NOT EXISTS idx_favorite_items_item_id
    ON favorite_items (item_id);


CREATE TABLE IF NOT EXISTS favorite_collections (
    user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    collection_id UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    PRIMARY KEY (user_id, collection_id)
);

CREATE INDEX IF NOT EXISTS idx_favorite_collections_collection_id
    ON favorite_collections (collection_id);


-- =========================================================
-- Comments
-- =========================================================

CREATE TABLE IF NOT EXISTS item_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,

    body VARCHAR(1000) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT chk_item_comments_body_not_blank
        CHECK (length(trim(body)) > 0)
);

CREATE INDEX IF NOT EXISTS idx_item_comments_item_id_created_at
    ON item_comments (item_id, created_at);

CREATE INDEX IF NOT EXISTS idx_item_comments_author_id
    ON item_comments (author_id);


CREATE TABLE IF NOT EXISTS collection_comments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    collection_id UUID NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    author_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,

    body VARCHAR(1000) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT chk_collection_comments_body_not_blank
        CHECK (length(trim(body)) > 0)
);

CREATE INDEX IF NOT EXISTS idx_collection_comments_collection_id_created_at
    ON collection_comments (collection_id, created_at);

CREATE INDEX IF NOT EXISTS idx_collection_comments_author_id
    ON collection_comments (author_id);


-- =========================================================
-- Private chats only
-- =========================================================

CREATE TABLE IF NOT EXISTS chats (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    first_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,
    second_user_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,

    first_user_last_read_at TIMESTAMPTZ,
    second_user_last_read_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),

    CONSTRAINT chk_chats_users_not_same
        CHECK (first_user_id <> second_user_id)
);

-- Prevent duplicate personal chats for the same pair:
-- user A + user B and user B + user A are treated as the same dialog.
CREATE UNIQUE INDEX IF NOT EXISTS ux_chats_private_pair
    ON chats (
        LEAST(first_user_id, second_user_id),
        GREATEST(first_user_id, second_user_id)
    );

CREATE INDEX IF NOT EXISTS idx_chats_first_user_id
    ON chats (first_user_id);

CREATE INDEX IF NOT EXISTS idx_chats_second_user_id
    ON chats (second_user_id);

CREATE INDEX IF NOT EXISTS idx_chats_updated_at
    ON chats (updated_at DESC);


CREATE TABLE IF NOT EXISTS messages (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    chat_id UUID NOT NULL REFERENCES chats(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES app_users(id) ON DELETE CASCADE,

    body VARCHAR(2000) NOT NULL,

    is_deleted BOOLEAN NOT NULL DEFAULT FALSE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ,

    CONSTRAINT chk_messages_body_not_blank
        CHECK (length(trim(body)) > 0)
);

CREATE INDEX IF NOT EXISTS idx_messages_chat_id_created_at
    ON messages (chat_id, created_at);

CREATE INDEX IF NOT EXISTS idx_messages_sender_id
    ON messages (sender_id);


-- =========================================================
-- Utility triggers
-- =========================================================

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


CREATE OR REPLACE FUNCTION ensure_message_sender_is_chat_participant()
RETURNS TRIGGER AS $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM chats c
        WHERE c.id = NEW.chat_id
          AND (c.first_user_id = NEW.sender_id OR c.second_user_id = NEW.sender_id)
    ) THEN
        RAISE EXCEPTION 'Message sender % is not a participant of chat %', NEW.sender_id, NEW.chat_id
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;


DROP TRIGGER IF EXISTS trg_app_users_updated_at ON app_users;
CREATE TRIGGER trg_app_users_updated_at
BEFORE UPDATE ON app_users
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();


DROP TRIGGER IF EXISTS trg_collections_updated_at ON collections;
CREATE TRIGGER trg_collections_updated_at
BEFORE UPDATE ON collections
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();


DROP TRIGGER IF EXISTS trg_items_updated_at ON items;
CREATE TRIGGER trg_items_updated_at
BEFORE UPDATE ON items
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();


DROP TRIGGER IF EXISTS trg_collection_ratings_updated_at ON collection_ratings;
CREATE TRIGGER trg_collection_ratings_updated_at
BEFORE UPDATE ON collection_ratings
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();


DROP TRIGGER IF EXISTS trg_chats_updated_at ON chats;
CREATE TRIGGER trg_chats_updated_at
BEFORE UPDATE ON chats
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();


DROP TRIGGER IF EXISTS trg_messages_updated_at ON messages;
CREATE TRIGGER trg_messages_updated_at
BEFORE UPDATE ON messages
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();


DROP TRIGGER IF EXISTS trg_messages_sender_is_participant ON messages;
CREATE TRIGGER trg_messages_sender_is_participant
BEFORE INSERT OR UPDATE OF chat_id, sender_id ON messages
FOR EACH ROW
EXECUTE FUNCTION ensure_message_sender_is_chat_participant();


-- =========================================================
-- Views for frontend statistics
-- =========================================================

CREATE OR REPLACE VIEW collection_stats AS
SELECT
    c.id AS collection_id,
    COALESCE(items_agg.item_count, 0) AS item_count,
    COALESCE(items_agg.total_price_amount, 0) AS total_price_amount,
    COALESCE(ratings_agg.avg_rating, 0) AS avg_rating,
    COALESCE(ratings_agg.rating_count, 0) AS rating_count,
    COALESCE(comments_agg.comment_count, 0) AS comment_count
FROM collections c
LEFT JOIN (
    SELECT
        collection_id,
        COUNT(*) AS item_count,
        SUM(COALESCE(price_amount, 0)) AS total_price_amount
    FROM items
    WHERE collection_id IS NOT NULL
    GROUP BY collection_id
) items_agg ON items_agg.collection_id = c.id
LEFT JOIN (
    SELECT
        collection_id,
        ROUND(AVG(rating)::numeric, 2) AS avg_rating,
        COUNT(*) AS rating_count
    FROM collection_ratings
    GROUP BY collection_id
) ratings_agg ON ratings_agg.collection_id = c.id
LEFT JOIN (
    SELECT
        collection_id,
        COUNT(*) AS comment_count
    FROM collection_comments
    GROUP BY collection_id
) comments_agg ON comments_agg.collection_id = c.id;


CREATE OR REPLACE VIEW user_profile_stats AS
WITH collection_count_agg AS (
    SELECT
        owner_id AS user_id,
        COUNT(*) AS collection_count
    FROM collections
    GROUP BY owner_id
),
item_count_agg AS (
    SELECT
        owner_id AS user_id,
        COUNT(*) AS item_count
    FROM items
    GROUP BY owner_id
),
collection_value_agg AS (
    SELECT
        c.owner_id AS user_id,
        SUM(COALESCE(i.price_amount, 0)) AS total_collections_price
    FROM collections c
    LEFT JOIN items i ON i.collection_id = c.id
    GROUP BY c.owner_id
),
item_likes_agg AS (
    SELECT
        i.owner_id AS user_id,
        COUNT(il.user_id) AS item_likes_count
    FROM items i
    LEFT JOIN item_likes il ON il.item_id = i.id
    GROUP BY i.owner_id
),
followers_agg AS (
    SELECT
        following_id AS user_id,
        COUNT(*) AS followers_count
    FROM user_follows
    GROUP BY following_id
),
following_agg AS (
    SELECT
        follower_id AS user_id,
        COUNT(*) AS following_count
    FROM user_follows
    GROUP BY follower_id
),
collection_rating_agg AS (
    SELECT
        c.owner_id AS user_id,
        ROUND(AVG(cr.rating)::numeric, 2) AS avg_collection_rating
    FROM collections c
    JOIN collection_ratings cr ON cr.collection_id = c.id
    GROUP BY c.owner_id
)
SELECT
    u.id AS user_id,
    COALESCE(cc.collection_count, 0) AS collection_count,
    COALESCE(ic.item_count, 0) AS item_count,
    COALESCE(il.item_likes_count, 0) AS item_likes_count,
    COALESCE(f.followers_count, 0) AS followers_count,
    COALESCE(fw.following_count, 0) AS following_count,
    COALESCE(cv.total_collections_price, 0) AS total_collections_price,
    COALESCE(cr.avg_collection_rating, 0) AS avg_collection_rating
FROM app_users u
LEFT JOIN collection_count_agg cc ON cc.user_id = u.id
LEFT JOIN item_count_agg ic ON ic.user_id = u.id
LEFT JOIN item_likes_agg il ON il.user_id = u.id
LEFT JOIN followers_agg f ON f.user_id = u.id
LEFT JOIN following_agg fw ON fw.user_id = u.id
LEFT JOIN collection_value_agg cv ON cv.user_id = u.id
LEFT JOIN collection_rating_agg cr ON cr.user_id = u.id;


CREATE OR REPLACE VIEW chat_list AS
SELECT
    c.id AS chat_id,
    c.first_user_id,
    first_user.username AS first_username,
    first_user.display_name AS first_display_name,
    c.second_user_id,
    second_user.username AS second_username,
    second_user.display_name AS second_display_name,
    c.first_user_last_read_at,
    c.second_user_last_read_at,
    c.created_at,
    c.updated_at,
    last_message.id AS last_message_id,
    last_message.sender_id AS last_message_sender_id,
    last_message.body AS last_message_body,
    last_message.created_at AS last_message_created_at
FROM chats c
JOIN app_users first_user ON first_user.id = c.first_user_id
JOIN app_users second_user ON second_user.id = c.second_user_id
LEFT JOIN LATERAL (
    SELECT m.*
    FROM messages m
    WHERE m.chat_id = c.id
      AND m.is_deleted = FALSE
    ORDER BY m.created_at DESC
    LIMIT 1
) last_message ON TRUE;


-- =========================================================
-- Initial categories
-- =========================================================

INSERT INTO categories (slug, title, sort_order)
VALUES
    ('figures', 'Фигурки', 10),
    ('coins', 'Монеты', 20),
    ('records', 'Пластинки', 30),
    ('books', 'Книги', 40),
    ('watches', 'Часы', 50),
    ('retro_tech', 'Ретро-техника', 60),
    ('table_games', 'Настольные игры', 70),
    ('minerals', 'Минералы', 80)
ON CONFLICT (slug) DO UPDATE
SET
    title = EXCLUDED.title,
    sort_order = EXCLUDED.sort_order;

COMMIT;
