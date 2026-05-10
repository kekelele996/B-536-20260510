# 🔧 修复指南 - 解决乱码和登录问题

## 问题说明

1. ❌ 页面显示中文乱码
2. ❌ admin 账号无法登录

## 解决方案

我已经修复了以下配置：

### ✅ 已修复的内容

1. **MySQL 字符编码**
   - 添加了 `--character-set-server=utf8mb4`
   - 添加了 `--collation-server=utf8mb4_unicode_ci`

2. **数据库连接字符串**
   - 添加了 `characterEncoding=utf8`
   - 添加了 `useUnicode=true`
   - 修改时区为 `Asia/Shanghai`

3. **Spring Boot 配置**
   - 强制使用 UTF-8 编码

4. **Nginx 配置**
   - 添加了 `charset utf-8`

## 🚀 立即修复步骤

### 方法 1：完整重建（推荐）

在终端执行以下命令：

```bash
cd /Users/alex.ni/ai/studySpace

# 停止并删除所有容器和数据
docker compose down -v

# 重新构建所有镜像（不使用缓存）
docker compose build --no-cache

# 启动所有服务
docker compose up -d

# 查看启动日志
docker compose logs -f
```

等待约 **1-2 分钟**后，访问 http://localhost

### 方法 2：快速测试

如果不想删除数据，可以只重启容器：

```bash
cd /Users/alex.ni/ai/studySpace

# 重新构建
docker compose build --no-cache

# 重启服务
docker compose restart

# 查看日志
docker compose logs -f backend
```

## 👤 登录账号

重建后，使用以下账号：

### 管理员账号
- 用户名：`admin`
- 密码：`admin123`

### 普通用户账号
- 用户名：`user1`
- 密码：`admin123`

### 或者注册新账号（推荐）

1. 访问 http://localhost
2. 点击"立即注册"
3. 填写信息（任意用户名和密码）
4. 使用新账号登录

## ✅ 验证修复

1. **检查字符编码**
   - 访问 http://localhost
   - 页面应该正确显示中文，不再乱码

2. **测试登录**
   - 使用 `admin` / `admin123` 登录
   - 或注册新用户后登录

3. **查看数据**
   - 登录后可以看到自习室列表
   - 中文名称应该正常显示

## 🔍 如果还有问题

### 查看后端日志
```bash
docker logs studyroom-backend --tail 50
```

### 查看前端日志
```bash
docker logs studyroom-frontend --tail 50
```

### 查看 MySQL 日志
```bash
docker logs studyroom-mysql --tail 50
```

### 检查字符集
```bash
docker exec -it studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom -e "SHOW VARIABLES LIKE 'character%';"
```

### 查看用户数据
```bash
docker exec -it studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom -e "SELECT username, role FROM user;"
```

## 💡 常见问题

### Q1: 重建后还是乱码？

清除浏览器缓存并刷新页面（Ctrl+Shift+R 或 Cmd+Shift+R）

### Q2: admin 还是登录不了？

使用注册功能创建新账号，这样密码加密一定匹配。

### Q3: 数据库连接失败？

等待 30 秒让 MySQL 完全启动，然后重启后端：
```bash
docker compose restart backend
```

## 📞 需要帮助？

如果以上方法都无法解决，请提供以下信息：

1. 后端日志：`docker logs studyroom-backend --tail 100`
2. 浏览器控制台错误信息（F12 查看）
3. 具体的错误提示
