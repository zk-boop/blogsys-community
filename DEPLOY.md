# 部署说明

## Docker Compose（推荐）

1. 复制 `.env.example` 为 `.env`，并修改数据库密码。
2. 在项目根目录运行：

   ```bash
   docker compose up -d --build
   ```

3. 浏览器访问 `http://服务器IP`。如修改了 `APP_PORT`，需在地址后添加端口。

查看状态和日志：

```bash
docker compose ps
docker compose logs -f backend frontend mysql
```

停止服务：

```bash
docker compose down
```

数据库和上传文件使用 Docker 卷持久化。只有首次创建数据库卷时会导入 `testxwf.sql`。
