-- 创建用户表
CREATE TABLE IF NOT EXISTS user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    role VARCHAR(20) DEFAULT 'USER',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建自习室表
CREATE TABLE IF NOT EXISTS study_room (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(200),
    capacity INT NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建时间段表
CREATE TABLE IF NOT EXISTS time_slot (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    study_room_id BIGINT NOT NULL,
    date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    available_seats INT NOT NULL,
    status VARCHAR(20) DEFAULT 'AVAILABLE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (study_room_id) REFERENCES study_room(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建预约表
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    seat_number INT,
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (time_slot_id) REFERENCES time_slot(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建候补队列表
CREATE TABLE IF NOT EXISTS waitlist (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    time_slot_id BIGINT NOT NULL,
    queue_position INT,
    status VARCHAR(20) DEFAULT 'WAITING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    FOREIGN KEY (time_slot_id) REFERENCES time_slot(id) ON DELETE CASCADE,
    UNIQUE KEY unique_waiting_user (user_id, time_slot_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 创建通知表
CREATE TABLE IF NOT EXISTS notification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    content VARCHAR(500) NOT NULL,
    type VARCHAR(50) DEFAULT 'SYSTEM',
    related_id BIGINT,
    status VARCHAR(20) DEFAULT 'UNREAD',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_user_status (user_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 插入测试管理员账号 (密码: admin123)
-- 注意：这个密码哈希是由 Spring Security BCryptPasswordEncoder 生成的
-- 如果登录失败，请使用注册功能创建新账号，或运行 fix-admin-simple.sh
INSERT INTO user (username, password, email, role) VALUES 
('admin', '$2a$10$OLZ7.ngiOQXiBepm6FTyB.CQUDqaTUfi/T5.7m/WCg8H7bZ29sPSW', 'admin@studyroom.com', 'ADMIN'),
('user1', '$2a$10$OLZ7.ngiOQXiBepm6FTyB.CQUDqaTUfi/T5.7m/WCg8H7bZ29sPSW', 'user1@test.com', 'USER');

-- 插入测试自习室数据
INSERT INTO study_room (name, location, capacity, description) VALUES 
('第一自习室', '图书馆1楼', 50, '安静明亮的自习环境'),
('第二自习室', '图书馆2楼', 40, '配有空调和独立座位'),
('第三自习室', '教学楼A座3楼', 30, '靠窗位置，采光良好');

-- 插入测试时间段数据 (未来7天)
INSERT INTO time_slot (study_room_id, date, start_time, end_time, available_seats) VALUES
-- 第一自习室
(1, CURDATE(), '08:00:00', '12:00:00', 50),
(1, CURDATE(), '14:00:00', '18:00:00', 50),
(1, CURDATE(), '19:00:00', '22:00:00', 50),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', 50),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '18:00:00', 50),
(1, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', '22:00:00', 50),
-- 第二自习室
(2, CURDATE(), '08:00:00', '12:00:00', 40),
(2, CURDATE(), '14:00:00', '18:00:00', 40),
(2, CURDATE(), '19:00:00', '22:00:00', 40),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', 40),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '18:00:00', 40),
(2, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', '22:00:00', 40),
-- 第三自习室
(3, CURDATE(), '08:00:00', '12:00:00', 30),
(3, CURDATE(), '14:00:00', '18:00:00', 30),
(3, CURDATE(), '19:00:00', '22:00:00', 30),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '08:00:00', '12:00:00', 30),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '14:00:00', '18:00:00', 30),
(3, DATE_ADD(CURDATE(), INTERVAL 1 DAY), '19:00:00', '22:00:00', 30);
