# BlogSys 备份与恢复手册

备份由数据库归档和上传文件归档组成。两者使用相同的上海时区时间戳，恢复时必须成对选择。

## 自动备份

Linux 手动执行：

```bash
cd /opt/blogsys
./deploy/backup/backup.sh
```

默认保留 7 份数据库归档和 3 份上传文件归档。可通过 `.env` 中的 `BACKUP_DIR`，或执行时的 `KEEP_DB`、`KEEP_UPLOADS` 调整。脚本使用 `umask 077`，并在任一步失败时返回非零状态。

定时任务：

```cron
15 3 * * * cd /opt/blogsys && /opt/blogsys/deploy/backup/backup.sh >> /var/log/blogsys-backup.log 2>&1
```

Windows 本地验证：

```powershell
$env:MYSQL_ROOT_PASSWORD='本地 root 密码'
$env:MYSQL_DATABASE='blog_sys'
.\deploy\backup\backup.ps1
```

## 检查备份

```bash
ls -lh /opt/blogsys/backups
gzip -t /opt/blogsys/backups/blogsys-db-时间戳.sql.gz
tar -tzf /opt/blogsys/backups/blogsys-uploads-时间戳.tar.gz | head
```

除本机归档外，至少再保留一份异机或对象存储副本。备份目录不得放入 Git。

## 恢复步骤

以下示例假定选择同一时间戳 `YYYYMMDD-HHMMSS`：

```bash
cd /opt/blogsys
export RESTORE_STAMP=YYYYMMDD-HHMMSS
test -f "backups/blogsys-db-$RESTORE_STAMP.sql.gz"
test -f "backups/blogsys-uploads-$RESTORE_STAMP.tar.gz"
docker compose stop frontend backend
```

1. 恢复数据库：

```bash
set -a
. ./.env
set +a
gzip -dc "backups/blogsys-db-$RESTORE_STAMP.sql.gz" \
  | docker compose exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql \
      mysql -uroot "$MYSQL_DATABASE"
```

2. 恢复对应上传文件。该步骤会先清空当前上传目录，因此必须确认归档时间戳正确：

```bash
docker compose exec -T backend sh -c 'find /app/uploads -mindepth 1 -maxdepth 1 -exec rm -rf -- {} +'
gzip -dc "backups/blogsys-uploads-$RESTORE_STAMP.tar.gz" \
  | docker compose exec -T backend tar -C /app/uploads -xf -
```

3. 启动并验证：

```bash
docker compose up -d backend frontend
docker compose ps
curl --fail http://127.0.0.1/health
```

最后抽查登录、文章列表、文章封面和头像。恢复失败时保持后端停止，保留当前错误日志和所选归档，不要继续覆盖备份。
