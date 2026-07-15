# 本地社区测试数据

## 内容范围

测试集包含 5 个普通用户、6 篇已发布文章、6 个补充标签，以及对应的点赞、收藏和评论。用户画像覆盖前端、后端、摄影旅行、家庭料理和阅读成长，页面上的统计数据与关联表保持一致。

所有测试账号密码均为：`password`

## 导入

先确认本地 MySQL 已创建并导入项目基础结构，然后在项目根目录运行：

```powershell
mysql -u root -p 你的数据库名 < project-boot/src/main/resources/demo-community-seed.sql
```

种子文件可重复执行，不会删除已有数据。封面 URL 默认指向运行在 `http://localhost:8080` 的后端文件接口；图片文件应位于 `project-boot/uploads/covers/`。

## 回滚

```powershell
mysql -u root -p 你的数据库名 < project-boot/src/main/resources/demo-community-seed-rollback.sql
```

回滚脚本只删除 ID 为 `9001–9099` 的演示数据。

## 注意

- 该文件用于本地展示和手工测试，不要作为生产初始化脚本。
- 不要把测试账号密码沿用到真实环境。
- 如果部署地址不是 `localhost:8080`，应统一调整种子文件中的封面 URL，或改用相对文件标识。
