#!/bin/bash

echo "==========================================="
echo "🔧 开始修复校园自习室预约系统"
echo "==========================================="
echo ""

echo "步骤 1/5: 停止所有容器并清除旧数据..."
docker compose down -v
echo "✅ 完成"
echo ""

echo "步骤 2/5: 重新构建所有镜像..."
docker compose build --no-cache
echo "✅ 完成"
echo ""

echo "步骤 3/5: 启动所有服务..."
docker compose up -d
echo "✅ 完成"
echo ""

echo "步骤 4/5: 等待服务启动（这需要约 60 秒）..."
echo "等待 MySQL 启动..."
sleep 20
echo "等待后端启动..."
sleep 30
echo "等待前端启动..."
sleep 10
echo "✅ 完成"
echo ""

echo "步骤 5/5: 检查服务状态..."
docker compose ps
echo ""

echo "==========================================="
echo "✅ 修复完成！"
echo "==========================================="
echo ""
echo "📱 访问地址："
echo "   前端: http://localhost"
echo "   后端: http://localhost:8080"
echo ""
echo "👤 默认账号："
echo "   用户名: admin"
echo "   密码: admin123"
echo ""
echo "   用户名: user1"
echo "   密码: admin123"
echo ""
echo "💡 提示："
echo "   1. 如果登录失败，请使用'注册'功能创建新账号"
echo "   2. 查看日志: docker compose logs -f"
echo "   3. 重新修复: ./fix-all.sh"
echo ""
