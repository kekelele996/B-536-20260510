# 快速启动指南

## ⚠️ 如果遇到乱码或登录问题

请查看 [FIX_GUIDE.md](FIX_GUIDE.md) 获取完整的修复方案。

**快速修复（推荐）：**
```bash
cd /Users/alex.ni/ai/studySpace
docker compose down -v
docker compose build --no-cache
docker compose up -d
```

## 一键启动命令

```bash
# 进入项目目录
cd /Users/alex.ni/ai/studySpace

# 启动所有服务（首次启动需要 5-10 分钟）
docker compose up -d

# 查看启动日志
docker compose logs -f
```

## 访问地址

- **前端系统**：http://localhost
- **后端 API**：http://localhost:8080
- **数据库**：localhost:3307

## 默认账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin  | admin123 | 管理员 |
| user1  | admin123 | 普通用户 |

## 验证系统

1. 打开浏览器访问 http://localhost
2. 使用 `admin` / `admin123` 登录
3. 可以看到自习室列表
4. 点击"管理后台"可以管理自习室和时间段

## 停止服务

```bash
# 停止所有服务
docker compose down

# 停止并删除数据
docker compose down -v
```

## 重新构建

```bash
# 修改代码后重新构建
docker compose up -d --build
```

## 常见问题

**Q: 启动后提示端口被占用？**

A: 修改 `docker-compose.yml` 中的端口映射

**Q: 前端无法访问？**

A: 等待 1-2 分钟让所有服务完全启动，或查看日志 `docker compose logs backend`

**Q: 数据库连接失败？**

A: 执行 `docker compose restart backend` 重启后端服务
