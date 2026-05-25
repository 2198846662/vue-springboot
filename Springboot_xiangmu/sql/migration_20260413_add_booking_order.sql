USE tourism;

CREATE TABLE IF NOT EXISTS booking_order (
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
    CONSTRAINT fk_booking_order_user FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_order_scenery FOREIGN KEY (scenery_id) REFERENCES scenery(id) ON DELETE CASCADE,
    CONSTRAINT fk_booking_order_reviewer FOREIGN KEY (reviewed_by) REFERENCES user(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_booking_order_user_status ON booking_order (user_id, status);
CREATE INDEX idx_booking_order_status ON booking_order (status);
CREATE INDEX idx_booking_order_scenery ON booking_order (scenery_id);
