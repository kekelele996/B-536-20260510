# 🚀 部署指南

## 在新机器上部署

### 前置要求

确保目标机器已安装：
- Docker
- Docker Compose

**不需要安装**：JDK、Node.js、MySQL（全部在容器中运行）

---

## 📦 部署步骤

### 1. 复制项目到目标机器

```bash
# 方式一：使用 git（推荐）
git clone <你的仓库地址>
cd studySpace

# 方式二：直接复制整个项目文件夹
scp -r studySpace user@target-server:/path/to/
```

### 2. 一键启动

```bash
cd studySpace

# 启动所有服务
docker compose up -d

# 查看启动日志
docker compose logs -f
```

等待约 **1-2 分钟**，直到看到所有服务启动成功。

### 3. 访问系统

- **前端地址**：http://目标机器IP
- **后端 API**：http://目标机器IP:8080

### 4. 登录测试

系统会**自动创建**默认管理员账号：

- 用户名：`admin`
- 密码：`admin123`

**✅ 无需手动配置，开箱即用！**

---

## 🔧 自动初始化功能

本系统在**首次启动时**会自动：

1. ✅ 创建数据库表结构
2. ✅ 插入测试数据（3 个自习室 + 时间段）
3. ✅ 创建默认管理员账号（username: admin, password: admin123）
4. ✅ 检查字符编码配置

如果 admin 账号已存在，系统会：
- 跳过创建
- 验证密码是否为 `admin123`
- 如果密码不匹配，会在日志中显示警告

---

## 🔐 密码工作原理

### 为什么之前密码有问题？

BCrypt 密码加密每次生成的哈希值都不同，必须使用**相同的编码器实例**来验证。

### 现在的解决方案

我们添加了 `DataInitializer.java`，它会在应用启动时：

1. 检查 admin 账号是否存在
2. 如果不存在，使用**当前运行的 BCryptPasswordEncoder** 生成密码
3. 这样保证了密码哈希和验证逻辑 100% 匹配

**代码位置**：`backend/src/main/java/com/studyroom/config/DataInitializer.java`

---

## 📋 部署验证清单

部署完成后，请验证以下功能：

### 基础功能
- [ ] 访问前端页面，中文显示正常（无乱码）
- [ ] 使用 `admin` / `admin123` 登录成功
- [ ] 可以看到自习室列表（应该有 3 个自习室）
- [ ] 点击自习室可以看到时间段

### 管理员功能
- [ ] 顶部导航显示"管理后台"按钮
- [ ] 可以添加新的自习室
- [ ] 可以为自习室添加时间段
- [ ] 可以查看所有预约记录

### 用户功能
- [ ] 注册新用户成功
- [ ] 新用户可以登录
- [ ] 可以预约自习室
- [ ] 可以查看我的预约
- [ ] 可以取消预约

---

## 🔧 常见部署问题

### Q1: 端口被占用

如果 80 或 8080 端口被占用，修改 `docker-compose.yml`：

```yaml
frontend:
  ports:
    - "8081:80"  # 修改为其他端口

backend:
  ports:
    - "8082:8080"  # 修改为其他端口
```

### Q2: 无法访问（防火墙）

确保目标机器的防火墙开放了相应端口：

```bash
# Ubuntu/Debian
sudo ufw allow 80
sudo ufw allow 8080

# CentOS/RHEL
sudo firewall-cmd --permanent --add-port=80/tcp
sudo firewall-cmd --permanent --add-port=8080/tcp
sudo firewall-cmd --reload
```

### Q3: admin 无法登录

**方案 1：查看后端日志**
```bash
docker logs studyroom-backend | grep -i "admin\|初始化\|password"
```

应该能看到：
```
✅ 默认管理员账号创建成功！
   用户名: admin
   密码: admin123
```

**方案 2：重新创建 admin 账号**
```bash
# 删除旧的 admin 账号
docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom -e "DELETE FROM user WHERE username = 'admin';"

# 重启后端（会自动重新创建）
docker compose restart backend

# 查看日志确认
docker logs studyroom-backend --tail 20
```

**方案 3：使用修复脚本**
```bash
./fix-admin-simple.sh
```

### Q4: 中文显示乱码

这不应该发生，因为我们已经配置了 UTF-8。如果出现，请：

1. 清除浏览器缓存（Ctrl+Shift+Delete）
2. 硬刷新页面（Ctrl+Shift+R）
3. 检查数据库字符集：
```bash
docker exec -it studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom -e "SHOW VARIABLES LIKE 'character%';"
```

应该都显示 `utf8mb4`。

---

## 🔄 更新部署

如果代码有更新，重新部署：

```bash
cd studySpace

# 拉取最新代码（如果使用 git）
git pull

# 停止旧容器
docker compose down

# 重新构建并启动
docker compose up -d --build

# 查看日志
docker compose logs -f
```

---

## 🗑️ 完全卸载

如果需要完全删除系统：

```bash
cd studySpace

# 停止并删除所有容器和数据
docker compose down -v

# 删除镜像（可选）
docker rmi studyspace-backend studyspace-frontend

# 删除项目文件夹
cd ..
rm -rf studySpace
```

---

## 📞 获取帮助

如果部署遇到问题：

1. 查看日志：`docker compose logs`
2. 检查容器状态：`docker compose ps`
3. 参考 FAQ：[FIX_GUIDE.md](FIX_GUIDE.md)

---

## ✅ 部署成功标志

看到以下内容说明部署成功：

```bash
$ docker compose ps
NAME                  STATUS
studyroom-backend     Up (healthy)
studyroom-frontend    Up
studyroom-mysql       Up (healthy)

$ docker logs studyroom-backend --tail 5
✅ 默认管理员账号创建成功！
   用户名: admin
   密码: admin123
Started StudyRoomApplication in 15.234 seconds
```

然后访问 http://localhost 即可使用！🎉
