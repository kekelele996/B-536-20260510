# 校园自习室预约系统

一个基于 Spring Boot + Vue 3 + MySQL 的完整自习室预约管理系统，支持用户预约、管理员管理等功能。

## 项目简介

本系统是一个完整的校园自习室预约解决方案，提供用户端和管理端两大功能模块：

- **用户端**：用户注册登录、浏览自习室、查看可预约时间段、预约座位、查看和取消预约
- **管理端**：自习室管理、时间段管理、预约记录查看

## 技术栈

### 后端
- **框架**：Spring Boot 3.1.5
- **JDK**：Java 17
- **ORM**：Spring Data JPA
- **数据库**：MySQL 8.0
- **构建工具**：Maven 3.9.5

### 前端
- **框架**：Vue 3.3.4
- **构建工具**：Vite 4.5.0
- **路由**：Vue Router 4.2.5
- **HTTP 客户端**：Axios 1.5.1
- **Web 服务器**：Nginx (Alpine)

### 容器化
- **Docker Engine**：任意版本
- **Docker Compose**：v3.8

## 项目结构

```
study-room-system/
├── docker-compose.yml          # Docker Compose 编排文件
├── README.md                   # 项目文档
├── db/                         # 数据库初始化脚本
│   └── init.sql               # MySQL 初始化 SQL
├── backend/                    # Spring Boot 后端
│   ├── Dockerfile             # 后端 Docker 镜像
│   ├── pom.xml                # Maven 依赖配置
│   └── src/
│       └── main/
│           ├── java/com/studyroom/
│           │   ├── StudyRoomApplication.java    # 主应用类
│           │   ├── common/
│           │   │   └── Result.java              # 统一返回格式
│           │   ├── config/
│           │   │   └── CorsConfig.java          # 跨域配置
│           │   ├── controller/                   # 控制器层
│           │   │   ├── UserController.java
│           │   │   ├── StudyRoomController.java
│           │   │   ├── TimeSlotController.java
│           │   │   └── ReservationController.java
│           │   ├── dto/                          # 数据传输对象
│           │   │   ├── LoginRequest.java
│           │   │   ├── RegisterRequest.java
│           │   │   ├── ReservationRequest.java
│           │   │   ├── ReservationVO.java
│           │   │   └── TimeSlotVO.java
│           │   ├── entity/                       # 实体类
│           │   │   ├── User.java
│           │   │   ├── StudyRoom.java
│           │   │   ├── TimeSlot.java
│           │   │   └── Reservation.java
│           │   ├── exception/                    # 异常处理
│           │   │   ├── BusinessException.java
│           │   │   └── GlobalExceptionHandler.java
│           │   ├── repository/                   # 数据访问层
│           │   │   ├── UserRepository.java
│           │   │   ├── StudyRoomRepository.java
│           │   │   ├── TimeSlotRepository.java
│           │   │   └── ReservationRepository.java
│           │   └── service/                      # 业务逻辑层
│           │       ├── UserService.java
│           │       ├── StudyRoomService.java
│           │       ├── TimeSlotService.java
│           │       └── ReservationService.java
│           └── resources/
│               └── application.yml               # Spring Boot 配置
└── frontend/                   # Vue 3 前端
    ├── Dockerfile             # 前端 Docker 镜像
    ├── nginx.conf             # Nginx 配置
    ├── package.json           # NPM 依赖配置
    ├── vite.config.js         # Vite 构建配置
    ├── index.html             # HTML 入口
    └── src/
        ├── main.js            # 应用入口
        ├── App.vue            # 根组件
        ├── style.css          # 全局样式
        ├── api/
        │   └── index.js       # API 接口封装
        ├── router/
        │   └── index.js       # 路由配置
        └── views/             # 页面组件
            ├── Login.vue          # 登录页
            ├── Register.vue       # 注册页
            ├── StudyRoomList.vue  # 自习室列表页
            ├── TimeSlotSelect.vue # 时间段选择页
            ├── MyReservations.vue # 我的预约页
            └── Admin.vue          # 管理后台页
```

## 快速开始

> 📖 **在新机器上部署？** 请查看 [DEPLOYMENT.md](DEPLOYMENT.md) 获取完整的部署指南

### 前置要求

- 已安装 Docker 和 Docker Compose
- **不需要**本地安装 JDK、Node.js 或 MySQL

### 一键启动

1. **克隆或进入项目目录**：
```bash
cd studySpace/
```

2. **启动所有服务**：
```bash
docker compose up -d
```

3. **等待服务启动**（首次启动需要下载镜像和构建，大约 5-10 分钟）：
```bash
# 查看启动日志
docker compose logs -f

# 检查服务状态
docker compose ps
```

4. **访问系统**：
   - **前端地址**：http://localhost （端口 80）(等待启动完成后一两分钟访问，期间会有数据库初始化操作)
   - **后端 API**：http://localhost:8080
   - **MySQL 数据库**：localhost:3307

### 停止服务

```bash
# 停止所有服务
docker compose down

# 停止并删除所有数据（包括数据库数据）
docker compose down -v
```

## 默认账号

系统在**首次启动时会自动创建**默认管理员账号：

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin  | admin123 | 管理员 | 自动创建，可访问管理后台 |
| user1  | admin123 | 普通用户 | 数据库初始化脚本创建 |

> 💡 **重要说明**：
> - admin 账号由后端启动时自动创建（`DataInitializer.java`）
> - 密码使用当前运行的 BCryptPasswordEncoder 加密，确保 100% 可用
> - 如果 admin 账号已存在，不会重复创建
> - 在任何环境部署都能直接使用 `admin` / `admin123` 登录

## 功能说明

### 用户端功能

1. **用户注册**：新用户可以注册账号
2. **用户登录**：使用用户名和密码登录系统
3. **浏览自习室**：查看所有可用的自习室列表
4. **选择时间段**：选择自习室后，查看可预约的时间段和剩余座位
5. **预约座位**：选择时间段进行预约，系统自动分配座位号
6. **查看预约**：查看自己的所有预约记录
7. **取消预约**：取消已预约但未过期的预约

### 管理端功能

1. **自习室管理**：
   - 添加新自习室（名称、位置、容量、描述）
   - 查看所有自习室
   - 删除自习室

2. **时间段管理**：
   - 为自习室添加可预约时间段
   - 设置日期、开始时间、结束时间、可用座位数
   - 删除时间段

3. **预约管理**：
   - 查看所有用户的预约记录
   - 查看预约状态（已预约、已取消）

## API 接口文档

### 用户接口 `/api/users`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/users/register` | 用户注册 |
| POST | `/api/users/login` | 用户登录 |
| GET  | `/api/users/{id}` | 获取用户信息 |

### 自习室接口 `/api/study-rooms`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/study-rooms` | 获取所有自习室 |
| GET  | `/api/study-rooms/{id}` | 获取自习室详情 |
| POST | `/api/study-rooms` | 创建自习室（管理员） |
| PUT  | `/api/study-rooms/{id}` | 更新自习室（管理员） |
| DELETE | `/api/study-rooms/{id}` | 删除自习室（管理员） |

### 时间段接口 `/api/time-slots`

| 方法 | 路径 | 说明 |
|------|------|------|
| GET  | `/api/time-slots/study-room/{studyRoomId}` | 获取自习室的时间段 |
| POST | `/api/time-slots` | 创建时间段（管理员） |
| DELETE | `/api/time-slots/{id}` | 删除时间段（管理员） |

### 预约接口 `/api/reservations`

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/reservations` | 创建预约 |
| GET  | `/api/reservations/user/{userId}` | 获取用户预约 |
| GET  | `/api/reservations/all` | 获取所有预约（管理员） |
| DELETE | `/api/reservations/{id}` | 取消预约 |

## 容器说明

### MySQL 容器 (studyroom-mysql)

- **镜像**：mysql:8.0
- **端口映射**：3307:3306
- **环境变量**：
  - `MYSQL_ROOT_PASSWORD`: rootpassword
  - `MYSQL_DATABASE`: studyroom
  - `MYSQL_USER`: studyroom_user
  - `MYSQL_PASSWORD`: studyroom_pass
- **数据持久化**：mysql-data volume
- **初始化脚本**：自动执行 `db/init.sql`

### 后端容器 (studyroom-backend)

- **基础镜像**：eclipse-temurin:17-jre-alpine
- **端口映射**：8080:8080
- **构建方式**：多阶段构建（Maven + JRE）
- **依赖**：等待 MySQL 健康检查通过

### 前端容器 (studyroom-frontend)

- **基础镜像**：nginx:alpine
- **端口映射**：80:80
- **构建方式**：多阶段构建（Node.js + Nginx）
- **反向代理**：自动代理 `/api` 请求到后端

## 常见问题 (FAQ)

### 1. 启动失败怎么办？

**问题**：执行 `docker compose up -d` 后服务无法启动

**解决方案**：
```bash
# 查看详细日志
docker compose logs

# 单独查看某个服务的日志
docker compose logs backend
docker compose logs frontend
docker compose logs mysql
```

### 2. 前端无法访问后端 API

**问题**：浏览器提示网络错误或 API 请求失败

**原因**：通常是后端容器还未完全启动或 MySQL 连接失败

**解决方案**：
```bash
# 检查后端日志
docker compose logs backend

# 确认后端是否正常运行
curl http://localhost:8080/api/study-rooms
```

### 3. 数据库连接失败

**问题**：后端日志显示 MySQL 连接错误

**解决方案**：
```bash
# 重启 MySQL 容器
docker compose restart mysql

# 等待 30 秒后重启后端
docker compose restart backend
```

### 4. 端口被占用

**问题**：提示 80 或 8080 端口已被占用

**解决方案**：
修改 `docker-compose.yml` 中的端口映射：
```yaml
# 例如将前端改为 8081 端口
frontend:
  ports:
    - "8081:80"
```

### 5. 如何清空所有数据重新开始？

```bash
# 停止并删除所有容器和数据卷
docker compose down -v

# 重新启动
docker compose up -d
```

### 6. 如何修改代码并重新部署？

```bash
# 修改代码后，重新构建并启动
docker compose up -d --build
```

### 7. 如何查看 MySQL 数据库？

```bash
# 方式一：使用 Docker 命令进入 MySQL
docker exec -it studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom

# 方式二：使用本地 MySQL 客户端连接
# Host: localhost
# Port: 3307
# User: studyroom_user
# Password: studyroom_pass
# Database: studyroom
```

### 8. 前端页面样式加载失败

**问题**：页面显示但没有样式

**解决方案**：清除浏览器缓存或使用无痕模式访问 http://localhost

### 9. 数据库初始化失败

**问题**：没有测试数据或表结构

**解决方案**：
```bash
# 完全清除并重建
docker compose down -v
docker compose up -d

# 手动执行初始化脚本
docker exec -i studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom < db/init.sql
```

### 10. 如何修改管理员密码？

数据库中的密码使用 BCrypt 加密，如需修改：

```bash
# 进入 MySQL
docker exec -it studyroom-mysql mysql -u studyroom_user -pstudyroom_pass studyroom

# 执行 SQL（密码需要重新加密）
UPDATE user SET password = '$2a$10$NEW_BCRYPT_HASH' WHERE username = 'admin';
```

## 开发说明

### 本地开发模式

如果需要本地开发（不使用 Docker）：

#### 后端开发
```bash
cd backend
mvn spring-boot:run
```

需要本地安装：
- JDK 17
- Maven 3.x
- MySQL 8.0（修改 application.yml 中的数据库连接）

#### 前端开发
```bash
cd frontend
npm install
npm run dev
```

需要本地安装：
- Node.js 18+
- npm

## 技术亮点

1. **完全容器化**：无需本地环境，一键启动
2. **多阶段构建**：优化镜像大小，加快构建速度
3. **健康检查**：确保数据库就绪后再启动后端
4. **数据持久化**：MySQL 数据保存在 Docker Volume
5. **反向代理**：Nginx 自动代理 API 请求，避免跨域
6. **统一返回格式**：后端使用标准的 Result 格式
7. **异常处理**：全局异常处理器统一处理错误
8. **现代化 UI**：渐变背景、卡片式设计、响应式布局
9. **Toast 消息提示**：替代 alert，提供更好的用户体验
10. **实时座位更新**：预约/取消时座位数实时同步
11. **友好错误提示**：将技术错误码转换为可理解的中文提示

## 最近更新

### v1.1.1 (2026-01-22)

- ✅ 修复用户可重复预约同一时间段的问题
- ✅ 已预约的时间段自动显示"已预约"并禁用
- ✅ 后端添加重复预约检查，确保数据正确性

### v1.1.0 (2026-01-22)

- ✅ 替换所有 alert 弹窗为现代化 Toast 组件
- ✅ 修复预约后座位数不更新的 Bug
- ✅ 优化错误提示，不再显示技术错误码
- ✅ 改进用户体验和交互流畅度

详见：[UPDATES.md](UPDATES.md) | [IMPROVEMENTS_SUMMARY.md](IMPROVEMENTS_SUMMARY.md) | [FIX_DUPLICATE_RESERVATION.md](FIX_DUPLICATE_RESERVATION.md)

## 系统截图说明

系统包含以下主要页面：
- **登录页**：渐变背景 + 居中卡片设计
- **注册页**：表单验证，支持邮箱和手机号
- **自习室列表**：网格布局展示所有自习室
- **时间段选择**：表格展示可预约时间，实时显示剩余座位
- **我的预约**：查看所有预约记录，支持取消
- **管理后台**：自习室管理、时间段管理、预约记录查看

## 许可证

本项目仅供学习和研究使用。

## 联系方式

如有问题或建议，欢迎提出 Issue。

---

**最后更新**：2026-01-21
