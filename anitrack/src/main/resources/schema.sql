CREATE TABLE IF NOT EXISTS user (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    created_at TEXT NOT NULL DEFAULT (datetime('now'))
);

CREATE TABLE IF NOT EXISTS anime (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES user(id),
    name TEXT NOT NULL,
    current_ep INTEGER NOT NULL DEFAULT 0,
    total_ep INTEGER,
    website_url TEXT NOT NULL,
    update_day INTEGER NOT NULL,
    rating INTEGER,
    last_watch_date TEXT,
    status TEXT NOT NULL DEFAULT 'watching'
);

CREATE TABLE IF NOT EXISTS checkin (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER NOT NULL REFERENCES user(id),
    check_date TEXT NOT NULL,
    count INTEGER NOT NULL DEFAULT 1,
    UNIQUE(user_id, check_date)
);

CREATE TABLE IF NOT EXISTS url_suggestion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_id INTEGER REFERENCES user(id),
    label TEXT NOT NULL,
    url TEXT NOT NULL,
    search_url TEXT,
    is_preset INTEGER NOT NULL DEFAULT 0,
    pinned INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_url_suggestion_user_id ON url_suggestion(user_id);

-- 预设通用网址
INSERT OR IGNORE INTO url_suggestion (id, user_id, label, url, search_url, is_preset) VALUES (1, NULL, '巴哈姆特动画疯', 'https://ani.gamer.com.tw/', 'https://ani.gamer.com.tw/search.php?keyword={keyword}', 1);
INSERT OR IGNORE INTO url_suggestion (id, user_id, label, url, search_url, is_preset) VALUES (2, NULL, 'Bilibili 番剧', 'https://www.bilibili.com/', 'https://search.bilibili.com/bangumi?keyword={keyword}', 1);
INSERT OR IGNORE INTO url_suggestion (id, user_id, label, url, search_url, is_preset) VALUES (3, NULL, 'YouTube', 'https://www.youtube.com/', 'https://www.youtube.com/results?search_query={keyword}', 1);
INSERT OR IGNORE INTO url_suggestion (id, user_id, label, url, search_url, is_preset) VALUES (4, NULL, 'Netflix', 'https://www.netflix.com/', 'https://www.netflix.com/search?q={keyword}', 1);
INSERT OR IGNORE INTO url_suggestion (id, user_id, label, url, search_url, is_preset) VALUES (5, NULL, 'Crunchyroll', 'https://www.crunchyroll.com/', 'https://www.crunchyroll.com/search?q={keyword}', 1);

-- 兼容旧表升级
ALTER TABLE anime ADD COLUMN status TEXT NOT NULL DEFAULT 'watching';
ALTER TABLE anime ADD COLUMN user_id INTEGER REFERENCES user(id);
ALTER TABLE anime ADD COLUMN cover_url TEXT;
ALTER TABLE url_suggestion ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0;