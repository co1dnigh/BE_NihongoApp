CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- Định danh & Xác thực (Hỗ trợ Local + OAuth2)
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NULL, 
    auth_provider ENUM('LOCAL', 'GOOGLE', 'FACEBOOK') DEFAULT 'LOCAL',
    provider_id VARCHAR(255) NULL,
    
    -- Thông tin hiển thị
    display_name VARCHAR(100) NOT NULL,
    avatar_url VARCHAR(255),
    role ENUM('LEARNER', 'ADMIN') DEFAULT 'LEARNER',
    
    -- Gamification (Level, Tiền tệ)
    level INT DEFAULT 1,
    exp INT DEFAULT 0,
    hearts INT DEFAULT 5,
    coins INT DEFAULT 0,
    current_streak INT DEFAULT 0,
    longest_streak INT DEFAULT 0,
    
    -- Optimistic Locking: Chống kẹt/âm tiền khi giao dịch đồng thời
    version INT DEFAULT 0,
    
    -- Audit
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME NULL DEFAULT NULL
);