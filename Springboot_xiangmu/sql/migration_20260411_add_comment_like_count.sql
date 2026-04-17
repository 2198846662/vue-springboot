-- 增量迁移：评论点赞数字段
-- 适用于已存在 tourism.comment 表的环境

USE tourism;

SET @col_exists := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = 'tourism'
      AND TABLE_NAME = 'comment'
      AND COLUMN_NAME = 'like_count'
);

SET @add_col_sql := IF(
    @col_exists = 0,
    'ALTER TABLE comment ADD COLUMN like_count INT DEFAULT 0 AFTER rating',
    'SELECT ''like_count already exists'''
);

PREPARE stmt FROM @add_col_sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

CREATE TABLE IF NOT EXISTS comment_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_comment_user (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
