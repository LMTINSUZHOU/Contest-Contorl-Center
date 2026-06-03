# 部署与启动说明

## 环境要求

- JDK 21
- Maven 3.9+
- Node.js 20+
- Docker / Docker Compose

## 启动 PostgreSQL

```bash
docker compose up -d postgres
```

默认连接信息：

- 数据库：`contest`
- 用户名：`contest`
- 密码：`contest`
- 端口：`5432`

## 启动后端

```bash
mvn spring-boot:run
```

可选环境变量：

```bash
DB_URL=jdbc:postgresql://localhost:5432/contest
DB_USERNAME=contest
DB_PASSWORD=contest
JWT_SECRET=replace-with-a-long-random-secret
ADMIN_EMAIL=admin@contest.local
ADMIN_PASSWORD=Admin@123456
CERTIFICATE_DIR=storage/certificates
```

首次启动会自动创建默认管理员。生产环境必须修改 `ADMIN_PASSWORD` 和 `JWT_SECRET`。

## 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端默认地址：`http://localhost:5173`  
后端默认地址：`http://localhost:8080`

开发环境 Vite 已配置 `/api` 代理到后端。若后端地址不同，可设置：

```bash
VITE_API_BASE_URL=http://localhost:8080 npm run dev
```

## 证书存储

证书默认保存到 `storage/certificates`。数据库只记录文件元数据和路径。后续迁移到 MinIO/S3 时，保留 `certificate_files` 表结构，替换 `CertificateService` 的存取逻辑即可。
