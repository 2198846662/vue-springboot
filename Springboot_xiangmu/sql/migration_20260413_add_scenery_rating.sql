-- Incremental migration: add scenery rating table for user-driven half-star rating
USE tourism;

CREATE TABLE IF NOT EXISTS scenery_rating (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    scenery_id BIGINT NOT NULL,
    score DECIMAL(2,1) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (scenery_id) REFERENCES scenery(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_scenery_rating (user_id, scenery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
