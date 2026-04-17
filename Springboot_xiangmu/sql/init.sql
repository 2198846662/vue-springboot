-- 创建数据库
CREATE DATABASE IF NOT EXISTS tourism DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE tourism;

-- 建表语句
DROP TABLE IF EXISTS favorite;
DROP TABLE IF EXISTS booking_order;
DROP TABLE IF EXISTS comment_like;
DROP TABLE IF EXISTS scenery_rating;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS scenery;
DROP TABLE IF EXISTS category;
DROP TABLE IF EXISTS user;

CREATE TABLE user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE,
    nickname VARCHAR(50),
    avatar VARCHAR(255),
    role VARCHAR(20) DEFAULT 'USER',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE category (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    icon VARCHAR(50),
    sort_order INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE scenery (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    cover_image VARCHAR(500) COMMENT '本地上传后保存访问路径，如 /uploads/xxx.jpg',
    location VARCHAR(200),
    ticket_price DECIMAL(10,2),
    open_time VARCHAR(50),
    latitude DECIMAL(10,6),
    longitude DECIMAL(10,6),
    category_id BIGINT,
    rating DECIMAL(3,1) DEFAULT 0,
    view_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (category_id) REFERENCES category(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE booking_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    scenery_id BIGINT NOT NULL,
    booking_days INT NOT NULL,
    traveler_count INT NOT NULL DEFAULT 1,
    travel_date DATE,
    note TEXT,
    unit_price DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    status VARCHAR(20) NOT NULL DEFAULT 'CART',
    paid_at DATETIME,
    reviewed_by BIGINT,
    reviewed_at DATETIME,
    reject_reason VARCHAR(255),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (scenery_id) REFERENCES scenery(id) ON DELETE CASCADE,
    FOREIGN KEY (reviewed_by) REFERENCES user(id) ON DELETE SET NULL,
    INDEX idx_booking_order_user_status (user_id, status),
    INDEX idx_booking_order_status (status),
    INDEX idx_booking_order_scenery (scenery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    content TEXT NOT NULL,
    scenery_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    rating INT,
    like_count INT DEFAULT 0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (scenery_id) REFERENCES scenery(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE comment_like (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    comment_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (comment_id) REFERENCES comment(id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    UNIQUE KEY uk_comment_user (comment_id, user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE scenery_rating (
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

CREATE TABLE favorite (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    scenery_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (scenery_id) REFERENCES scenery(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_scenery (user_id, scenery_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 初始化分类数据
INSERT INTO category (name, description, icon, sort_order) VALUES
('自然风光', '山水湖泊、森林草原等自然景观', 'nature', 1),
('人文景观', '历史古迹、文化遗址等', 'culture', 2),
('主题公园', '游乐场、主题乐园', 'amusement', 3),
('博物馆', '各类博物馆、展览馆', 'museum', 4),
('古镇村落', '古镇、古村落、乡村旅游', 'village', 5);

-- 初始化景点示例数据
-- cover_image 预留本地路径，未上传时可为空（前端会显示占位图）
INSERT INTO scenery (name, description, cover_image, location, ticket_price, open_time, latitude, longitude, category_id, rating) VALUES
('黄山风景区', '中国著名的山岳风景区，以奇松、怪石、云海、温泉著称', NULL, '安徽省黄山市', 230.00, '06:30-17:30', 30.131129, 118.182763, 1, 4.8),
('故宫博物院', '明清两代的皇家宫殿，世界上现存规模最大、保存最为完整的木质结构古建筑之一', NULL, '北京市东城区', 60.00, '08:30-17:00', 39.916345, 116.397155, 2, 4.9),
('迪士尼乐园', '亚洲最大的主题公园之一，适合家庭游玩', NULL, '上海市浦东新区', 399.00, '09:00-20:00', 31.144339, 121.657410, 3, 4.7);

-- 注意：用户密码现在使用 BCrypt 加密，请通过注册接口创建新用户
-- 注册后管理员账号需要手动将 role 字段更新为 'ADMIN'
