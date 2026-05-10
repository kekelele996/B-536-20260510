# 使用多阶段构建
FROM maven:3.9.5-eclipse-temurin-17-alpine AS build
WORKDIR /app

# 复制 pom.xml 并下载依赖
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源代码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# 运行阶段
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# 复制构建的 jar 文件
COPY --from=build /app/target/study-room-system.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]
