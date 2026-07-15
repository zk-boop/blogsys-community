# BlogSys 生产部署手册

目标环境：阿里云 ECS，2 vCPU、2 GiB 内存、40 GiB 系统盘、3 Mbps 公网带宽。部署目录示例为 `/opt/blogsys`。

## 1. 云侧准备

安全组入方向只开放：

| 端口 | 用途 | 建议来源 |
|---:|---|---|
| 22/TCP | SSH 运维 | 仅管理员固定 IP |
| 80/TCP | HTTP / ACME | 全网 |
| 443/TCP | HTTPS | 全网 |

不要开放 3306 或 8080。应用的 MySQL 和 Spring Boot 只在 Docker 内部网络中暴露。

## 2. 安装 Docker

Ubuntu：

```bash
sudo apt-get update
sudo apt-get install -y ca-certificates curl
curl -fsSL https://get.docker.com | sudo sh
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

Alibaba Cloud Linux 3：

```bash
sudo dnf install -y docker docker-compose-plugin
sudo systemctl enable --now docker
sudo usermod -aG docker "$USER"
```

重新登录 SSH 后验证：

```bash
docker version
docker compose version
```

如果系统仓库没有 Compose 插件，按 Docker 官方 Linux 安装说明补装 `docker-compose-plugin`，不要回退到旧的 Python `docker-compose`。

## 3. 获取代码与生成配置

```bash
sudo mkdir -p /opt/blogsys
sudo chown "$USER":"$USER" /opt/blogsys
git clone https://github.com/zk-boop/blogsys-community.git /opt/blogsys
cd /opt/blogsys
cp .env.example .env
chmod 600 .env
```

生成两个不同的长随机密码：

```bash
openssl rand -base64 36
openssl rand -base64 36
```

编辑 `.env`，至少修改数据库 root 密码、应用账户密码、域名与 CORS 来源。备案和 HTTPS 完成后设置：

```dotenv
APP_DOMAIN=你的备案域名
CORS_ALLOWED_ORIGINS=https://你的备案域名
SESSION_COOKIE_SECURE=true
```

需要初始化永久管理员时，同时设置 `DEMO_SEED_ENABLED=true` 和 `DEMO_ADMIN_PASSWORD`。首次成功启动后可将 `DEMO_SEED_ENABLED` 改回 `false`；初始化器按用户名幂等，不会覆盖已有账号密码。

## 4. 首次启动与验收

```bash
cd /opt/blogsys
docker compose config --quiet
docker compose up -d --build
docker compose ps
docker compose logs --tail=200 mysql backend frontend
curl --fail http://127.0.0.1/health
```

验收要点：

- 三个服务均为 `healthy`；
- `/health` 返回 Spring Boot `UP`；
- 公网只能访问 80/443，3306/8080 无法直连；
- 注册、登录、投稿、评论和图片上传正常；
- 非管理员不能发布文章或进入管理接口；
- 重建容器后数据库和上传图片仍存在。

## 5. 域名、备案与 HTTPS

备案完成后，将域名 A 记录指向 ECS 公网 IP。签发证书前先保持当前 HTTP 配置运行，然后短暂停止前端并用 Certbot standalone 签发：

```bash
cd /opt/blogsys
docker compose stop frontend
sudo mkdir -p /etc/letsencrypt
sudo docker run --rm -p 80:80 \
  -v /etc/letsencrypt:/etc/letsencrypt \
  certbot/certbot certonly --standalone \
  -d "你的备案域名" --agree-tos -m "你的联系邮箱" --no-eff-email
```

使用 HTTPS 覆盖文件启动：

```bash
docker compose -f docker-compose.yml -f docker-compose.https.yml up -d --build
curl --fail "https://你的备案域名/health"
```

`deploy/nginx/https.conf.template` 由 Nginx 官方镜像在启动时通过 `APP_DOMAIN` 渲染。配置会将 HTTP 跳转到 HTTPS，并只读挂载 `/etc/letsencrypt`。

## 6. 日常升级与回滚

升级前先执行备份：

```bash
cd /opt/blogsys
./deploy/backup/backup.sh
git fetch origin
git pull --ff-only origin main
docker compose up -d --build
curl --fail http://127.0.0.1/health
```

回滚代码：

```bash
git log --oneline -10
git checkout <已验证的提交哈希>
docker compose up -d --build
```

如果升级包含不兼容数据变更，按 [BACKUP.md](BACKUP.md) 使用同一时间戳的数据库与上传文件成对恢复。不要使用 `docker compose down -v`，该命令会删除持久化数据卷。

## 7. 资源与日志检查

```bash
docker stats --no-stream
docker compose logs --since=30m backend
df -h
du -sh /opt/blogsys/backups
```

当前预算：MySQL 640 MiB、后端 512 MiB（Java 最大堆 384 MiB）、前端 128 MiB。容器日志单文件 10 MiB、最多 3 个文件。
