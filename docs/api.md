# 学科竞赛管理系统 API 文档

后端默认地址：`http://localhost:8080`  
Swagger UI：`http://localhost:8080/swagger-ui.html`

所有受保护接口需要请求头：

```http
Authorization: Bearer <jwt-token>
```

## 认证

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/auth/login` | 登录，只有 `NORMAL` 状态账号可登录 |
| POST | `/api/auth/register/student` | 学生邮箱注册，默认 `PENDING` |
| POST | `/api/auth/register/teacher` | 指导老师邮箱注册，默认 `PENDING` |
| GET | `/api/auth/me` | 获取当前登录用户 |

## 管理员

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/dashboard/summary` | 后台首页汇总统计 |
| GET | `/api/admin/statistics/awards?dimension=year` | 获奖统计，支持 `year`、`competitionGrade`、`awardLevel`、`subjectType` |
| GET | `/api/admin/users/pending` | 待审核注册用户 |
| POST | `/api/admin/users/{id}/approve` | 审核通过用户 |
| POST | `/api/admin/users/{id}/reject` | 驳回用户 |
| POST | `/api/admin/users/{id}/disable` | 禁用用户 |
| GET/POST | `/api/admin/students` | 查询/新增学生 |
| GET/PUT/DELETE | `/api/admin/students/{id}` | 学生详情/更新/删除 |
| POST | `/api/admin/students/{id}/password` | 重置学生账号密码 |
| GET/POST | `/api/admin/teachers` | 查询/新增指导老师 |
| GET/PUT/DELETE | `/api/admin/teachers/{id}` | 指导老师详情/更新/删除 |
| POST | `/api/admin/teachers/{id}/password` | 重置教师账号密码 |
| GET/POST | `/api/admin/competitions` | 查询/新增竞赛 |
| PUT/DELETE | `/api/admin/competitions/{id}` | 更新/删除竞赛 |
| GET/POST | `/api/admin/competition-tracks` | 竞赛赛道查询/新增 |
| PUT/DELETE | `/api/admin/competition-tracks/{id}` | 更新/删除赛道 |
| GET/POST | `/api/admin/teams` | 团队查询/新增 |
| GET/PUT/DELETE | `/api/admin/teams/{id}` | 团队详情/更新/删除 |
| GET/POST | `/api/admin/awards` | 获奖查询/录入 |
| GET/PUT/DELETE | `/api/admin/awards/{id}` | 获奖详情/自由更新/删除 |
| GET | `/api/admin/award-declarations` | 待审核获奖申报 |
| POST | `/api/admin/award-declarations/{id}/approve` | 审核通过申报 |
| POST | `/api/admin/award-declarations/{id}/reject` | 驳回申报 |

获奖录入和学生申报提交字段包括：`competitionId`、`trackId`、`competitionAlias`、`subjectType`、`awardLevel`、学生或团队、`advisorTeacherIds`、`awardDate`、`awardLocation`。竞赛等级继承自竞赛基础信息，不在赛道或获奖中单独维护。

## 学生

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/student/profile` | 学生个人资料 |
| GET | `/api/student/awards` | 已审核通过的个人/团队获奖 |
| GET | `/api/student/award-declarations` | 本人申报记录 |
| POST | `/api/student/award-declarations` | 提交获奖申报 |
| PUT | `/api/student/award-declarations/{id}` | 编辑本人或所在团队的获奖记录，提交后变为待审核 |
| DELETE | `/api/student/award-declarations/{id}` | 删除本人或所在团队的获奖记录 |

学生申报时从 `GET /api/catalog/tracks?competitionId={competitionId}` 获取可选赛道。团队赛必须选择已建立团队；个人赛直接使用当前学生身份申报。

## 公共目录

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/catalog/competitions` | 可用竞赛列表 |
| GET | `/api/catalog/tracks?competitionId={competitionId}` | 指定竞赛下的赛道选项 |
| GET | `/api/catalog/teachers` | 指导老师选项 |
| GET | `/api/catalog/teams` | 团队选项 |

## 指导老师

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/teacher/profile` | 教师个人资料 |
| GET | `/api/teacher/awards` | 指导记录和教师本人获奖 |
| GET | `/api/teacher/guidance` | 指导记录别名接口 |
| GET | `/api/teacher/award-declarations` | 本人指导的待审核申报 |
| POST | `/api/teacher/award-declarations/{id}/approve` | 指导老师审核通过 |
| POST | `/api/teacher/award-declarations/{id}/reject` | 指导老师驳回 |

## 证书

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| POST | `/api/certificates/awards/{awardId}` | 上传获奖证书，支持 PDF/JPG/PNG |
| GET | `/api/certificates/{certificateId}/download` | 下载证书 |
| POST | `/api/admin/certificates/{certificateId}/replace` | 管理员替换证书 |

后台获奖页面的“导入证书”使用 `POST /api/certificates/awards/{awardId}`，再次上传会自动将旧证书置为非当前有效版本。

证书下载规则：

- 管理员可下载全部证书。
- 学生只能下载本人个人赛或所在团队的已审核通过证书。
- 指导老师只能下载本人指导或本人获奖的已审核通过证书。
- 普通用户不能下载未审核通过获奖记录的证书。

## 导入导出

| 方法 | 路径 | 说明 |
| --- | --- | --- |
| GET | `/api/admin/import-export/{type}/template` | 下载模板 |
| POST | `/api/admin/import-export/{type}/import` | 上传 Excel 导入 |
| GET | `/api/admin/import-export/{type}/export` | 导出当前数据 |
| GET | `/api/admin/import-export/{type}/import-errors/{jobId}` | 下载导入错误报告 |

`type` 支持：`students`、`teachers`、`competitions`、`awards`。
