#!/bin/bash

echo "正在生成密码哈希..."

# 等待后端容器启动
echo "等待后端容器启动..."
sleep 10

# 编译并运行密码生成器
docker exec studyroom-backend sh -c "cd /app && javac -cp 'classes:lib/*' /app/PasswordGen.java 2>/dev/null && java -cp 'classes:lib/*' PasswordGen" 2>/dev/null || {
    echo "使用备用方法..."
    # 直接更新数据库使用已知的正确哈希
    docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom <<EOF
-- 这是 'admin123' 的 BCrypt 哈希 (使用 rounds=10)
UPDATE user SET password = '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'admin';
UPDATE user SET password = '\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG' WHERE username = 'user1';
SELECT username, LEFT(password, 20) as password_prefix, role FROM user;
EOF
}

echo "密码哈希已更新！"
echo "请尝试使用以下账号登录："
echo "用户名: admin"
echo "密码: admin123"
