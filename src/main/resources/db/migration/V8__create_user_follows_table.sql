CREATE TABLE user_follows (
    follower_id BIGINT NOT NULL COMMENT 'ID của người đi theo dõi',
    followed_id BIGINT NOT NULL COMMENT 'ID của người được theo dõi',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Khóa chính kép: Đảm bảo A chỉ có thể follow B một lần duy nhất
    PRIMARY KEY (follower_id, followed_id),
    
    -- Khóa ngoại trỏ về bảng users
    FOREIGN KEY (follower_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (followed_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Tạo thêm index để tối ưu tốc độ khi đếm số lượng người theo dõi (followers)
CREATE INDEX idx_followed ON user_follows(followed_id);