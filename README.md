# BlogSys Community

BlogSys 是一个面向多用户内容社区的博客发布与审核系统，也是一个可公开部署的全栈简历项目。访客可注册、投稿、评论、点赞、收藏和上传图片；文章经管理员审核后公开展示。界面借鉴成熟内容社区的信息组织方式，但保留克制、干净、偏编辑感的视觉体系。

## 在线演示

域名正在完成实名与备案流程。演示地址将在 ICP 备案、DNS 和 HTTPS 配置完成后补充。

## 技术架构

```text
浏览器
  └─ Nginx / Vue 3 SPA（唯一公网入口）
       └─ /api → Spring Boot 2.7 / Spring Security / JDBC Session
                    └─ MySQL 8
```

- 后端：Java 8、Spring Boot 2.7、Spring Security、Spring Data JPA、Spring Session JDBC、Actuator
- 前端：Vue 3、Vue Router、Vuex、Element Plus、Axios
- 交付：Docker Compose、Nginx、MySQL 8、持久化卷、健康检查、日志轮转
- 测试：JUnit 5、MockMvc、H2、Mockito、Jest

## 功能矩阵

| 模块 | 普通访客/用户 | 管理员 |
|---|---|---|
| 账号 | 注册、登录、资料维护 | 封禁、启用、删除用户 |
| 文章 | 草稿、投稿、查看自己的待审核内容 | 审核、发布、退回、推荐、置顶 |
| 互动 | 评论、回复、点赞、收藏 | 评论审核与删除 |
| 内容组织 | 浏览分类与标签、申请新增 | 分类与标签审核维护 |
| 文件 | 上传头像和文章封面 | 删除不合规文件 |

## 安全与公开演示策略

- Session + CSRF；写操作身份只取服务端认证上下文，不信任前端传入的用户 ID 或角色。
- 草稿和待审核文章仅作者与管理员可读；发布、退回和用户管理仅管理员可操作。
- 注册、登录、投稿、评论和上传分别限流，超限统一返回 HTTP 429。
- 上传只接受扩展名、MIME 与文件头一致的 JPEG、PNG、WebP，单文件上限 5 MiB。
- 公开注册账号标记为 `EPHEMERAL`，账号及关联投稿、评论、点赞、收藏在 7 天后自动清理。
- 管理员和可选演示账号标记为 `PERMANENT`，密码只通过环境变量注入。
- MySQL 与后端不映射公网端口；生产环境 Hibernate 仅校验结构，不自动改表。

## 目录结构

```text
project-boot/       Spring Boot API、后端测试与镜像
project-vue/       Vue 3 SPA、前端测试与 Nginx 镜像
deploy/mysql/      无账号数据的数据库结构基线
deploy/nginx/      HTTPS 配置模板
deploy/backup/     Linux 与 Windows 备份脚本
docs/              架构、重构、设计和实施记录
docs/screenshots/  项目截图存放目录
```

## 本地启动

依赖：JDK 8、MySQL 8、Node.js 20+。

1. 创建本地数据库 `blog_sys`，按顺序导入：

   - `deploy/mysql/01-schema.sql`
   - `deploy/mysql/02-session-schema.sql`

2. 配置数据库环境变量，再启动后端：

```powershell
$env:DB_URL='jdbc:mysql://localhost:3306/blog_sys?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
$env:DB_USERNAME='root'
$env:DB_PASSWORD='你的本地数据库密码'
cd project-boot
.\mvnw.cmd spring-boot:run
```

3. 启动前端：

```powershell
cd project-vue
npm ci
npm run serve
```

默认前端地址为 `http://localhost:8081`，后端为 `http://localhost:8080`。全新数据库没有内置管理员；本地需要演示账号时，将 `DEMO_SEED_ENABLED=true` 并通过环境变量提供管理员和演示用户密码。

## 测试与构建

```powershell
cd project-boot
.\mvnw.cmd clean test
.\mvnw.cmd -DskipTests package

cd ..\project-vue
npm ci
npm run test:unit
npm run build
```

## 部署与运维

- [生产部署手册](DEPLOY.md)
- [备份与恢复手册](BACKUP.md)
- [本轮生产化设计](docs/superpowers/specs/2026-07-15-portfolio-deployment-hardening-design.md)
- [实施计划与验证项](docs/superpowers/plans/2026-07-15-portfolio-deployment-hardening.md)

## License

[MIT](LICENSE)
