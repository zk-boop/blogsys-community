# BlogSys 后端模块化重构

## 目标

在保持现有 Vue 调用可迁移的前提下，将后端重构为身份可信、权限明确、事务完整、测试隔离、文档随代码演进的模块化单体。

## 目标调用链

```text
HTTP 请求
  -> 认证与授权
  -> Controller 校验请求 DTO
  -> Application Service 开启事务
  -> Domain Policy 校验所有权与状态转换
  -> Repository 持久化聚合
  -> Response DTO 与正确 HTTP 状态
```

## 模块与状态

| 编号 | 模块 | 范围 | 状态 | 文档 |
|---|---|---|---|---|
| 00 | 基线 | 技术栈、接口、依赖与风险清单 | 已完成 | [00-baseline.md](00-baseline.md) |
| 01 | 公共基础与测试 | 配置、异常、响应、测试数据库 | 已完成 | [modules/01-foundation.md](modules/01-foundation.md) |
| 02 | 认证与用户 | 注册、登录、Session、资料、密码、角色 | 首轮完成 | [modules/02-auth-user.md](modules/02-auth-user.md) |
| 03 | 文章 | 文章 CRUD、所有权、状态、标签事务 | 首轮完成 | [modules/03-article.md](modules/03-article.md) |
| 04 | 评论 | 评论、回复、审核、计数 | 首轮完成 | [modules/04-comment.md](modules/04-comment.md) |
| 05 | 互动 | 点赞、收藏、幂等与并发计数 | 首轮完成 | [modules/05-interaction.md](modules/05-interaction.md) |
| 06 | 分类与标签 | 分类树、标签、审核权限 | 首轮完成 | [modules/06-taxonomy.md](modules/06-taxonomy.md) |
| 07 | 文件 | 上传、下载、删除、路径与所有权 | 首轮完成 | [modules/07-file.md](modules/07-file.md) |
| 08 | 数据与交付 | 迁移、部署配置、回归与发布门禁 | 部分完成 | [modules/08-data-delivery.md](modules/08-data-delivery.md) |

## 强制约束

- 写操作的用户身份只能来自服务端认证上下文。
- Controller 不直接操作 Repository，不接收可变 JPA 实体作为最终形态的请求模型。
- 所有权与管理员权限必须在 Service 事务边界内再次校验。
- 一个业务命令及其关联表、冗余计数必须处于同一事务。
- 每个模块必须记录接口兼容性、数据影响、验证命令和剩余风险。
- 自动化测试不得依赖共享远程数据库或提交到仓库的真实凭据。

## 实施顺序

按编号推进。若模块间存在依赖，先提供兼容适配，再迁移前端消费者，最后删除旧参数或旧鉴权代码。

## 本轮验收

- 后端：`mvn clean test`，34 项测试通过，0 failure，0 error。
- 前端：`npm run build` 通过；保留 vendor 包体积告警作为性能优化项。
- 详细记录：[verification.md](verification.md)。
