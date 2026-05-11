#!/bin/bash

echo "正在修复数据库密码..."

# 停止所有容器并删除数据卷
docker compose down -v

# 重新启动（会重新初始化数据库）
docker compose up -d

echo "修复完成！等待服务启动..."
echo "大约需要 30-60 秒"
echo ""
echo "你可以运行以下命令查看启动进度："
echo "docker compose logs -f"
