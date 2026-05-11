#!/bin/bash

echo "🔧 快速修复 admin 账号"
echo ""

# 查看当前所有用户
echo "当前系统中的用户："
docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom -e "SELECT id, username, role FROM user;"

echo ""
echo "--------------------------------"
echo ""

# 方案：删除旧的 admin，让用户重新注册
echo "正在删除旧的 admin 和 user1 账号..."
docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom <<'EOF'
DELETE FROM user WHERE username IN ('admin', 'user1');
EOF

echo "✅ 旧账号已删除"
echo ""
echo "================================"
echo "📝 请按以下步骤操作："
echo "================================"
echo ""
echo "1. 访问 http://localhost"
echo "2. 点击'立即注册'"
echo "3. 填写以下信息："
echo ""
echo "   用户名: admin"
echo "   密码: admin123"
echo "   邮箱: admin@studyroom.com"
echo ""
echo "4. 注册成功后，回到这里按任意键继续..."
echo ""
read -p "按回车继续..." 

echo ""
echo "正在将 admin 设置为管理员角色..."
docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom <<'EOF'
UPDATE user SET role = 'ADMIN' WHERE username = 'admin';
SELECT username, role, email FROM user;
EOF

echo ""
echo "================================"
echo "✅ 修复完成！"
echo "================================"
echo ""
echo "现在可以使用以下账号登录："
echo "  用户名: admin"
echo "  密码: admin123"
echo ""
echo "登录后可以访问'管理后台'功能！"
echo ""
