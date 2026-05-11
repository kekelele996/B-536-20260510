#!/bin/bash

echo "正在更新数据库密码..."

# 直接更新数据库
docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom <<'EOF'
-- 更新为新的 BCrypt 哈希 (密码: admin123)
UPDATE user SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyMv.5rYyp7m' WHERE username = 'admin';
UPDATE user SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5gyMv.5rYyp7m' WHERE username = 'user1';

-- 显示更新后的用户信息
SELECT username, LEFT(password, 30) as password_start, role FROM user;
EOF

echo ""
echo "✅ 密码已更新！"
echo ""
echo "请尝试登录："
echo "  用户名: admin"
echo "  密码: admin123"
echo ""
echo "  用户名: user1"
echo "  密码: admin123"
