# 构建阶段
FROM node:18-alpine AS build
WORKDIR /app

# 复制 package.json 并安装依赖
COPY package.json ./
RUN npm install

# 复制源代码并构建
COPY . .
RUN npm run build

# 运行阶段
FROM nginx:alpine
WORKDIR /usr/share/nginx/html

# 删除默认的 nginx 静态资源
RUN rm -rf ./*

# 复制构建结果
COPY --from=build /app/dist .

# 复制 nginx 配置
COPY nginx.conf /etc/nginx/conf.d/default.conf

# 暴露端口
EXPOSE 80

# 启动 nginx
CMD ["nginx", "-g", "daemon off;"]
