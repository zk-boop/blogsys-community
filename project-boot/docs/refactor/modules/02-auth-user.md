# 02 认证与用户模块

## 目标

- 使用 Spring Security 作为唯一认证上下文，保留 JDBC Session。
- Session 仅保存稳定主体信息，不保存 JPA 实体图。
- 资料与密码修改只能作用于当前用户；管理员能力显式授权。

## 计划

- 建立 `security` 包、当前用户主体和统一 401/403 响应。
- 重构注册、登录、退出、当前用户接口到应用服务。
- 登录成功后轮换 Session ID；封禁用户的已有会话失效或重新校验状态。
- 引入 `UpdateProfileRequest`、`ChangePasswordRequest`，密码不再通过查询参数传递。
- 修复管理员封禁/解封的 Principal 不一致问题。

## 兼容策略

先保留现有 URL；旧的用户 ID 路径参数仅用于兼容并必须与当前用户一致。前端迁移后再提供 `/api/users/me` 形式接口并淘汰旧写法。

## 验收矩阵

覆盖匿名、本人、其他用户、禁用用户、管理员五类主体。

## 已实施

- Spring Security 取代自定义拦截器，统一输出 JSON 401/403；Session 中仅保存稳定的安全主体，不再保存 JPA `User` 实体。
- 登录使用 BCrypt 和 Session 固定攻击防护；每次请求重新校验数据库中的用户状态和角色，封禁或角色变更可使旧会话失效。
- 启用 Cookie CSRF，登录和注册保持可直接调用；新增 `GET /api/auth/csrf`，Vue 登录成功后初始化 `XSRF-TOKEN`。
- 资料修改执行本人或管理员校验；密码只允许本人修改，优先接收 JSON，请求查询参数仅作旧客户端兼容。
- `/api/admin/**` 使用角色授权，修复原 Principal 类型不一致造成的封禁/解封失效。

## 验证与剩余风险

- `SecurityBoundaryTest` 覆盖登录 Session、匿名写入、CSRF、普通用户访问后台、本人/他人资料和文章所有权等 10 个边界。
- 后续需要增加“最后一个管理员不可被封禁”和多节点 Session 失效策略。
