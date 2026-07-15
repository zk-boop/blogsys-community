# 08 数据与交付模块

## 目标

- 使用 Flyway 或 Liquibase 管理生产数据库结构。
- 生产环境关闭 `ddl-auto=update`，改为 `validate` 或 `none`。
- 配置按 local/test/staging/production 分离，密钥只通过环境或密钥服务注入。
- 建立后端、前端、迁移和安全回归的发布门禁。

## 迁移要求

- 对现有数据库执行基线，不重放带业务数据的 SQL 导出。
- 每个迁移记录前向步骤、回滚或补偿操作。
- 明确用户删除与文章、评论保留策略后再调整级联关系。

## 发布门禁

- 空库和代表性旧库均能迁移。
- 后端测试、前端构建和权限矩阵通过。
- CORS、Cookie、CSRF、HTTPS 与反向代理配置在部署拓扑中验证。

## 已实施

- 新增 `application-prod.properties`：生产环境使用 `ddl-auto=validate`，关闭 SQL 自动初始化和 Session schema 自动创建。
- 数据库密码不再提供仓库内默认值；Cookie Secure/SameSite 和 CORS 允许源改为环境配置。
- 测试改为 H2 MySQL 兼容模式及确定性种子数据，完全隔离远程数据库。
- 后端全量测试与前端生产构建已通过，具体见 `../verification.md`。

## 未完成门禁

- 尚未引入 Flyway/Liquibase，也未对代表性旧 MySQL 数据库执行基线迁移；因此生产数据迁移不能标记完成。
- 发布前必须补做备份恢复演练、真实 MySQL 集成测试、HTTPS/反向代理下的 Cookie/CSRF 验证。
