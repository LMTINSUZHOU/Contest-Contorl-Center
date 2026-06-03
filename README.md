# 学科竞赛管理系统

本项目是一个用于管理高校学科竞赛全过程信息的系统，包含 Spring Boot 后端、React 前端、PostgreSQL 数据库、证书本地存储、导入导出、审核流程和统计看板。

## 快速开始

```bash
docker compose up -d postgres
mvn spring-boot:run
```

前端：

```bash
cd frontend
npm install
npm run dev
```

默认管理员：

- 邮箱：`admin@contest.local`
- 密码：`Admin@123456`

生产环境请通过环境变量修改默认管理员密码和 JWT 密钥。

## 文档

- API 文档：[docs/api.md](docs/api.md)
- ER 图：[docs/er-diagram.mmd](docs/er-diagram.mmd)
- 部署说明：[docs/deployment.md](docs/deployment.md)
- 导入导出说明：[docs/import-export.md](docs/import-export.md)
