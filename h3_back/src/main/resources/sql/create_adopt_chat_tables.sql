-- 입양 상담 채팅 세션 테이블
CREATE TABLE IF NOT EXISTS adopt_chat_sessions (
    session_id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(100) NOT NULL,
    session_title VARCHAR(500) DEFAULT '새로운 상담',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    expires_at TIMESTAMP DEFAULT (DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY)),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_updated_at (updated_at)
);

-- 입양 상담 채팅 메시지 테이블
CREATE TABLE IF NOT EXISTS adopt_chat_messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(255) NOT NULL,
    sender_type ENUM('USER', 'BOT') NOT NULL,
    message_text TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    metadata JSON,
    INDEX idx_session_id (session_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (session_id) REFERENCES adopt_chat_sessions(session_id) ON DELETE CASCADE
);
