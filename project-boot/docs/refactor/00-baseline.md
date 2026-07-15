# 00 当前基线

## 技术基线

- Java 8
- Spring Boot 2.7.14
- Spring MVC、Spring Data JPA、Spring Session JDBC
- MySQL 8 方言
- BCrypt
- Vue + Axios，跨域请求携带 Cookie

## 当前模块

```text
controller -> service -> repository -> model -> MySQL
                 |
                 +-> local file storage
AuthInterceptor -> selected URL patterns -> JDBC Session
```

## 关键接口兼容面

- `/api/auth/**`：登录、注册、退出、当前用户。
- `/api/users/**`：用户查询、资料和密码。
- `/api/articles/**`：文章查询、创建、编辑和状态转换。
- `/api/comments/**`：评论、回复、审核和点赞计数。
- `/api/likes/**`、`/api/favorites/**`：文章互动。
- `/api/categories/**`、`/api/tags/**`：内容分类。
- `/api/files/**`：头像与封面。
- `/api/admin/**`：审核及后台管理。

## 风险基线

| 等级 | 风险 | 后续模块 |
|---|---|---|
| P0 | 文章、评论等写接口可匿名调用并冒充请求中的用户 ID | 02–07 |
| P0 | 测试配置包含共享远程数据库凭据 | 01 |
| P0 | 管理员封禁/解封混用 Session 与 Principal，功能失效 | 02 |
| P1 | Session 保存整个 User 实体，封禁和角色变化可能不及时生效 | 02 |
| P1 | 点赞记录和计数、文章和标签不在同一事务 | 03、05 |
| P1 | 文件写接口缺少认证、类型与所有权检查 | 07 |
| P2 | Controller 绑定实体并承担业务编排 | 02–07 |
| P2 | 失败响应状态不一致，部分返回内部异常信息 | 01–07 |

## 基线验证

- 包根路径：`com.zk.projectboot`
- 干净编译：通过。
- 原完整测试：依赖不可用远程数据库，ApplicationContext 无法启动。
- 前端兼容约束：当前创建文章和评论仍传递 `authorId/userId`，移除前需同步更新 Vue API 层。
