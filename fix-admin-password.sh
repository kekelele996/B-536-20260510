#!/bin/bash

echo "🔧 修复管理员账号密码"
echo "================================"
echo ""

# 方案：从新注册的用户获取正确的密码哈希格式，然后更新到 admin
# 或者直接创建新的 admin 账号

echo "请选择修复方案："
echo ""
echo "方案 1: 删除旧 admin，重新注册 admin 账号（推荐）"
echo "方案 2: 从已注册用户复制密码到 admin"
echo ""
read -p "请输入 1 或 2: " choice

if [ "$choice" = "1" ]; then
    echo ""
    echo "步骤 1: 删除旧的 admin 和 user1 账号..."
    docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom <<EOF
DELETE FROM reservation WHERE user_id IN (SELECT id FROM user WHERE username IN ('admin', 'user1'));
DELETE FROM user WHERE username IN ('admin', 'user1');
SELECT '✅ 旧账号已删除' as status;
EOF
    
    echo ""
    echo "步骤 2: 现在请按以下步骤操作："
    echo ""
    echo "  1. 访问 http://localhost"
    echo "  2. 点击'立即注册'"
    echo "  3. 填写信息："
    echo "     - 用户名: admin"
    echo "     - 密码: admin123 (或你自己设置的密码)"
    echo "     - 邮箱: admin@studyroom.com"
    echo "  4. 注册成功后，不要登录，继续下一步"
    echo ""
    read -p "注册完成后按回车继续..."
    
    echo ""
    echo "步骤 3: 将 admin 账号设置为管理员角色..."
    docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom <<EOF
UPDATE user SET role = 'ADMIN' WHERE username = 'admin';
SELECT username, role, email FROM user WHERE username = 'admin';
EOF
    
    echo ""
    echo "✅ 完成！admin 账号已设置为管理员"
    echo "现在可以使用 admin 账号登录了！"

elif [ "$choice" = "2" ]; then
    echo ""
    echo "查询已注册的用户..."
    docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom -e "SELECT id, username, role FROM user WHERE username NOT IN ('admin', 'user1');"
    
    echo ""
    read -p "请输入你刚注册的用户名: " new_user
    
    echo ""
    echo "正在复制密码哈希到 admin 账号..."
    docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom <<EOF
UPDATE user SET password = (SELECT password FROM (SELECT password FROM user WHERE username = '$new_user') AS temp) WHERE username = 'admin';
SELECT '✅ 密码已更新' as status;
SELECT username, LEFT(password, 30) as password_hash, role FROM user WHERE username IN ('admin', '$new_user');
EOF
    
    echo ""
    echo "✅ 完成！"
    echo "现在 admin 的密码已经和 $new_user 的密码一样了"
    echo "请使用你注册 $new_user 时的密码来登录 admin"
    
else
    echo "无效的选择"
    exit 1
fi

echo ""
echo "================================"
