# BlogSys Portfolio Deployment Hardening Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn BlogSys into a public, rate-limited, self-cleaning portfolio application that runs reproducibly on a 2 vCPU / 2 GiB Alibaba Cloud ECS and is published as a safe GitHub monorepo.

**Architecture:** A single root Git repository contains the Spring Boot API, Vue SPA, deployment files, and documentation. Nginx is the only public container, proxies `/api` to a memory-bounded Spring Boot service, and serves cached static assets; MySQL is internal-only. Server-side Session/CSRF remains the authentication model, while in-process rate limiting, explicit retention policy, scheduled cleanup, and host-side backups protect the public demo.

**Tech Stack:** Java 8, Spring Boot 2.7.14, Spring Security, Spring Session JDBC, Spring Data JPA, MySQL 8, H2 tests, Vue 3.2, Vue CLI 5, Axios, Element Plus, Docker Compose, Nginx 1.27, GitHub.

## Global Constraints

- Target host is Alibaba Cloud ECS: 2 vCPU, 2 GiB RAM, 40 GiB ESSD, 3 Mbps bandwidth.
- Keep Java 8, Spring Boot 2.7.14, Vue 3.2, and Vue CLI 5; no framework-major upgrades.
- Keep server-side JDBC Session and CSRF; do not add JWT.
- Anonymous users may read published content; authenticated visitors may register, submit articles, comment, like, favorite, and upload images.
- Ephemeral visitor accounts and their owned data expire seven days after registration; protected seed data never expires.
- Only JPEG, PNG, and WebP uploads are accepted, with a 5 MiB per-file limit.
- GitHub remote is `https://github.com/zk-boop/blogsys-community.git`, default branch `main`, repository public, license MIT.
- Do not commit real credentials, local user data, uploads, database dumps, build output, IDE metadata, cookies, Session IDs, or fixed production IPs.
- Follow red-green-refactor for production behavior; configuration-only work uses executable validation commands.

---

## File Structure

### Root delivery files

- Create `.gitignore`: one ignore policy for backend, frontend, deployment, IDE, uploads, and secrets.
- Create `README.md`: portfolio-facing product and engineering overview.
- Create `LICENSE`: MIT license.
- Modify `.env.example`: production variable contract with non-secret placeholders.
- Modify `docker-compose.yml`: internal network, health checks, resource budgets, log rotation, application DB user, and backup profile.
- Modify `DEPLOY.md`: ECS, Docker, TLS, upgrade, and acceptance workflow.
- Create `BACKUP.md`: backup and restore runbook.
- Create `deploy/mysql/01-schema.sql`: sanitized empty-database baseline.
- Create `deploy/mysql/02-session-schema.sql`: Spring Session schema for MySQL.
- Create `deploy/backup/backup.ps1`: local Windows verification helper.
- Create `deploy/backup/backup.sh`: ECS database/uploads backup and retention script.
- Create `deploy/nginx/https.conf.template`: post-ICP TLS server template.

### Backend units

- Modify `project-boot/pom.xml`: Actuator dependency only; rate limiting stays dependency-free.
- Modify `project-boot/src/main/resources/application.properties`: safe local defaults and 5 MiB uploads.
- Modify `project-boot/src/main/resources/application-prod.properties`: validate-only schema, proxy headers, health endpoint, cleanup and Cookie production settings.
- Modify `project-boot/src/main/java/com/zk/projectboot/security/SecurityConfig.java`: public health endpoint and rate-limit filter placement.
- Create `project-boot/src/main/java/com/zk/projectboot/ratelimit/RateLimitPolicy.java`: immutable path/method/limit/window definition.
- Create `project-boot/src/main/java/com/zk/projectboot/ratelimit/RateLimitService.java`: clock-driven fixed-window counters.
- Create `project-boot/src/main/java/com/zk/projectboot/ratelimit/RateLimitFilter.java`: resolve trusted client identity, choose policy, and emit 429 API response.
- Modify `project-boot/src/main/java/com/zk/projectboot/service/impl/FileStorageServiceImpl.java`: signature validation and GIF removal.
- Modify `project-boot/src/main/java/com/zk/projectboot/model/User.java`: explicit `RetentionPolicy`.
- Modify `project-boot/src/main/java/com/zk/projectboot/application/AuthApplicationService.java`: all public registrations become `EPHEMERAL`.
- Modify `project-boot/src/main/java/com/zk/projectboot/repository/UserRepository.java`: cleanup query.
- Create `project-boot/src/main/java/com/zk/projectboot/retention/VisitorCleanupReport.java`: cleanup result counts and file names.
- Create `project-boot/src/main/java/com/zk/projectboot/retention/VisitorDataCleanupService.java`: transactional selection and deletion.
- Create `project-boot/src/main/java/com/zk/projectboot/retention/VisitorDataCleanupScheduler.java`: daily scheduling and post-commit file deletion.
- Create `project-boot/src/main/java/com/zk/projectboot/config/TimeConfig.java`: injectable Asia/Shanghai clock.
- Create `project-boot/src/main/java/com/zk/projectboot/config/DemoDataInitializer.java`: optional protected demo accounts/data using environment-supplied passwords.

### Backend tests

- Extend `project-boot/src/test/java/com/zk/projectboot/security/SecurityBoundaryTest.java`: complete anonymous/owner/non-owner/admin boundary.
- Create `project-boot/src/test/java/com/zk/projectboot/ratelimit/RateLimitServiceTest.java`.
- Create `project-boot/src/test/java/com/zk/projectboot/ratelimit/RateLimitFilterTest.java`.
- Create `project-boot/src/test/java/com/zk/projectboot/service/FileStorageServiceImplTest.java`.
- Create `project-boot/src/test/java/com/zk/projectboot/retention/VisitorDataCleanupServiceTest.java`.
- Create `project-boot/src/test/java/com/zk/projectboot/config/DemoDataInitializerTest.java`.
- Create `project-boot/src/test/resources/application-prod-test.properties`.

### Frontend units

- Modify `project-vue/package.json`: add Vue CLI Jest unit-test script and matching Vue CLI 5 test dependencies.
- Create `project-vue/src/api/errorPolicy.js`: map 401/403/429 responses to Session/UI actions.
- Create `project-vue/tests/unit/errorPolicy.spec.js`: behavior tests for error mapping.
- Modify `project-vue/src/api/index.js`: use the error policy and remove debug output.
- Modify `project-vue/src/api/config.js`: production always uses `/api` unless an explicit build-time override is supplied.
- Modify image templates in `ArticleCard.vue`, `Home.vue`, `Search.vue`, `ArticlesByCategory.vue`, and `ArticlesByTag.vue`: lazy loading and explicit dimensions.
- Replace `project-vue/src/assets/default-cover.png` with `project-vue/src/assets/default-cover.webp` and update imports.
- Modify `project-vue/nginx.conf`: gzip, immutable hashed assets, no-cache HTML, proxy headers, upload limit, and health path.

---

### Task 1: Establish the Safe GitHub Monorepo Baseline

**Files:**
- Create: `.gitignore`
- Create: `LICENSE`
- Create: `README.md`
- Modify: `.env.example`
- Preserve backup: `%TEMP%/blogsys-git-metadata-20260715.zip`

**Interfaces:**
- Consumes: the current dirty backend and frontend working trees as the source of truth.
- Produces: one root `main` branch with `origin=https://github.com/zk-boop/blogsys-community.git` and no nested repositories.

- [ ] **Step 1: Record and back up both nested repository states**

Run from the workspace root:

```powershell
$root = (Resolve-Path '.').Path
$backendGit = (Resolve-Path 'project-boot/.git').Path
$frontendGit = (Resolve-Path 'project-vue/.git').Path
if (-not $backendGit.StartsWith($root) -or -not $frontendGit.StartsWith($root)) { throw 'Unsafe Git metadata path' }
git -C project-boot status --short | Out-File "$env:TEMP/blogsys-backend-status.txt" -Encoding utf8
git -C project-vue status --short | Out-File "$env:TEMP/blogsys-frontend-status.txt" -Encoding utf8
Compress-Archive -LiteralPath project-boot/.git,project-vue/.git -DestinationPath "$env:TEMP/blogsys-git-metadata-20260715.zip" -Force
```

Expected: the ZIP exists and both status snapshots are non-empty.

- [ ] **Step 2: Write the root ignore policy**

Create `.gitignore` with these exact categories:

```gitignore
.idea/
.vscode/
*.iml
*.log
.env
.env.*
!.env.example
**/target/
**/node_modules/
**/dist/
project-boot/uploads/
**/*.sql.gz
**/*.tar.gz
backups/
.DS_Store
Thumbs.db
```

- [ ] **Step 3: Add the MIT license and safe environment contract**

Create `LICENSE` using the standard MIT text with `Copyright (c) 2026 zk-boop`.

Set `.env.example` to:

```dotenv
APP_HTTP_PORT=80
APP_HTTPS_PORT=443
APP_DOMAIN=example.com
MYSQL_DATABASE=blog_sys
MYSQL_APP_USER=blogsys
MYSQL_APP_PASSWORD=replace-with-a-long-random-password
MYSQL_ROOT_PASSWORD=replace-with-a-different-long-random-password
SESSION_COOKIE_SECURE=false
CORS_ALLOWED_ORIGINS=http://localhost
DEMO_SEED_ENABLED=false
DEMO_USER_PASSWORD=replace-only-when-demo-seeding-is-enabled
CLEANUP_ENABLED=true
BACKUP_DIR=/opt/blogsys/backups
```

- [ ] **Step 4: Run the pre-publication secret and large-file gate**

Run:

```powershell
rg -n --hidden -g '!**/.git/**' -g '!**/node_modules/**' -g '!**/target/**' "118\.178\.235\.178|DB_PASSWORD\s*=\s*[^$]|MYSQL_ROOT_PASSWORD:\s*[^$]|BEGIN (RSA|OPENSSH|EC) PRIVATE KEY|BLOGSESSION=" .
Get-ChildItem -Recurse -File | Where-Object { $_.Length -gt 10MB -and $_.FullName -notmatch '\\(node_modules|target|dist|\.git)\\' } | Select-Object FullName,Length
```

Expected: no secrets; every file over 10 MiB is either removed from publication or explicitly justified before continuing.

- [ ] **Step 5: Remove nested metadata only after verifying the backup**

Run:

```powershell
if (-not (Test-Path "$env:TEMP/blogsys-git-metadata-20260715.zip")) { throw 'Git metadata backup missing' }
$targets = @((Resolve-Path 'project-boot/.git').Path, (Resolve-Path 'project-vue/.git').Path)
foreach ($target in $targets) {
  if (-not $target.StartsWith((Resolve-Path '.').Path)) { throw "Unsafe target: $target" }
  Remove-Item -LiteralPath $target -Recurse -Force
}
if (Test-Path '.git') { Remove-Item -LiteralPath (Resolve-Path '.git').Path -Recurse -Force }
```

Expected: `project-boot/.git` and `project-vue/.git` no longer exist; source files are unchanged.

- [ ] **Step 6: Initialize and validate the root repository**

Run:

```powershell
git init -b main
git remote add origin https://github.com/zk-boop/blogsys-community.git
git add .
git status --short
git diff --cached --check
git ls-files | rg "(^|/)(node_modules|target|dist|uploads|\.env)(/|$)"
```

Expected: staged source/docs/deployment files; diff check passes; final command returns no matches.

- [ ] **Step 7: Commit the imported baseline but do not push yet**

```powershell
git commit -m "chore: establish BlogSys monorepo baseline"
```

Expected: one root commit on `main`; remote remains unmodified until all publication gates pass.

---

### Task 2: Make Production Configuration Reproducible

**Files:**
- Modify: `project-boot/pom.xml`
- Modify: `project-boot/src/main/resources/application.properties`
- Modify: `project-boot/src/main/resources/application-prod.properties`
- Modify: `project-boot/src/main/java/com/zk/projectboot/security/SecurityConfig.java`
- Create: `deploy/mysql/01-schema.sql`
- Create: `deploy/mysql/02-session-schema.sql`

**Interfaces:**
- Produces: `/actuator/health`, a validate-only `prod` profile, and empty-database SQL consumed by Docker Compose.

- [ ] **Step 1: Record executable production-configuration assertions**

Run before editing:

```powershell
$prod = Get-Content -Raw project-boot/src/main/resources/application-prod.properties
if ($prod -notmatch 'spring\.jpa\.hibernate\.ddl-auto=validate') { throw 'Production schema mode is not validate' }
if ($prod -notmatch 'spring\.sql\.init\.mode=never') { throw 'Production SQL init is not disabled' }
if ($prod -match 'password\s*=\s*[^$\r\n]') { throw 'Literal production password found' }
if ($prod -notmatch 'management\.endpoints\.web\.exposure\.include=health') { throw 'Health endpoint configuration missing' }
```

Expected: the first three gates pass and the health-endpoint gate fails, proving the missing deployment requirement.

- [ ] **Step 2: Add Actuator and production-safe properties**

Add `spring-boot-starter-actuator`; configure:

```properties
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=never
server.forward-headers-strategy=native
spring.jpa.hibernate.ddl-auto=validate
spring.sql.init.mode=never
spring.servlet.multipart.max-file-size=5MB
spring.servlet.multipart.max-request-size=5MB
cleanup.enabled=${CLEANUP_ENABLED:true}
demo.seed.enabled=${DEMO_SEED_ENABLED:false}
```

Permit only `/actuator/health` publicly in `SecurityConfig`.

- [ ] **Step 3: Create the sanitized baseline SQL**

Derive `deploy/mysql/01-schema.sql` from the current JPA mappings and existing local schema, containing roles, users, categories, tags, articles, article_tags, comments, article_likes, article_favorites, indexes, and foreign keys. Add `retention_policy VARCHAR(16) NOT NULL DEFAULT 'EPHEMERAL'` to `users`. Copy the MySQL-compatible Session DDL to `02-session-schema.sql`.

No `INSERT INTO users` statement is allowed in either schema file.

- [ ] **Step 4: Verify the SQL against a disposable MySQL 8 container**

```powershell
docker run --name blogsys-schema-test -e MYSQL_ROOT_PASSWORD=test-root -e MYSQL_DATABASE=blog_sys -d mysql:8.0 --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
docker exec blogsys-schema-test mysqladmin ping -uroot -ptest-root --wait=30
Get-Content -Raw deploy/mysql/01-schema.sql | docker exec -i blogsys-schema-test mysql -uroot -ptest-root blog_sys
Get-Content -Raw deploy/mysql/02-session-schema.sql | docker exec -i blogsys-schema-test mysql -uroot -ptest-root blog_sys
docker exec blogsys-schema-test mysql -uroot -ptest-root -N -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='blog_sys';"
docker rm -f blogsys-schema-test
```

Expected: imports exit 0 and table count is at least 12.

- [ ] **Step 5: Verify backend tests and commit**

```powershell
./mvnw.cmd test
git add project-boot/pom.xml project-boot/src/main deploy/mysql
git commit -m "build: add reproducible production schema and health checks"
```

Expected: Maven exits 0.

---

### Task 3: Add Test-Driven Public-Demo Rate Limiting

**Files:**
- Create: `project-boot/src/main/java/com/zk/projectboot/ratelimit/RateLimitPolicy.java`
- Create: `project-boot/src/main/java/com/zk/projectboot/ratelimit/RateLimitService.java`
- Create: `project-boot/src/main/java/com/zk/projectboot/ratelimit/RateLimitFilter.java`
- Modify: `project-boot/src/main/java/com/zk/projectboot/security/SecurityConfig.java`
- Create: `project-boot/src/test/java/com/zk/projectboot/ratelimit/RateLimitServiceTest.java`
- Create: `project-boot/src/test/java/com/zk/projectboot/ratelimit/RateLimitFilterTest.java`

**Interfaces:**
- Produces: `boolean RateLimitService.tryAcquire(String key, int limit, Duration window)` and JSON HTTP 429 responses.

- [ ] **Step 1: Write the failing fixed-window unit tests**

Tests must prove:

```java
assertTrue(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));
assertTrue(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));
assertFalse(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));
clock.advance(Duration.ofHours(1));
assertTrue(service.tryAcquire("register:127.0.0.1", 2, Duration.ofHours(1)));
```

Run:

```powershell
./mvnw.cmd -Dtest=RateLimitServiceTest test
```

Expected: test compilation fails because `RateLimitService` does not exist.

- [ ] **Step 2: Implement the minimal clock-driven service**

Use `ConcurrentHashMap<String, WindowCounter>`, compute `windowStart = nowMillis - floorMod(nowMillis, windowMillis)`, and replace stale counters atomically. Inject `Clock`; do not use `System.currentTimeMillis()` directly.

- [ ] **Step 3: Verify green and add filter-level failing tests**

Filter tests must prove registration request 6 returns 429, distinct IP remains allowed, and authenticated comment limits key by principal ID. The 429 body must satisfy:

```java
jsonPath("$.success").value(false);
jsonPath("$.message").value("操作过于频繁，请稍后重试");
```

- [ ] **Step 4: Implement exact policies and trusted proxy handling**

Implement policies:

```java
POST /api/auth/register       IP, 5/hour
POST /api/auth/login          IP, 20/10 minutes
POST /api/articles            user, 10/day
PATCH /api/articles/*/submit  user, 10/day
POST /api/comments            user, 30/hour
POST /api/files/upload/*      user, 20/hour
```

Use `request.getRemoteAddr()` after Nginx/native forward-header processing; never read arbitrary `X-Forwarded-For` directly inside the filter.

- [ ] **Step 5: Run focused and full tests, then commit**

```powershell
./mvnw.cmd -Dtest=RateLimitServiceTest,RateLimitFilterTest,SecurityBoundaryTest test
./mvnw.cmd test
git add project-boot/src/main/java/com/zk/projectboot/ratelimit project-boot/src/main/java/com/zk/projectboot/security project-boot/src/test/java/com/zk/projectboot/ratelimit
git commit -m "feat: rate limit public demo mutations"
```

Expected: both commands exit 0.

---

### Task 4: Harden Image Upload Validation

**Files:**
- Modify: `project-boot/src/main/java/com/zk/projectboot/service/impl/FileStorageServiceImpl.java`
- Modify: `project-boot/src/main/java/com/zk/projectboot/controller/FileUploadController.java`
- Create: `project-boot/src/test/java/com/zk/projectboot/service/FileStorageServiceImplTest.java`

**Interfaces:**
- Produces: storage accepts only JPEG/PNG/WebP whose declared type, extension, and magic bytes agree.

- [ ] **Step 1: Write failing tests for content signatures**

Create `MockMultipartFile` cases proving:

```java
valid JPEG FF D8 FF -> accepted
valid PNG 89 50 4E 47 0D 0A 1A 0A -> accepted
valid WebP RIFF....WEBP -> accepted
GIF -> rejected
text bytes named photo.png -> rejected
PNG bytes declared image/jpeg -> rejected
../photo.png -> stored under generated UUID only
```

Run:

```powershell
./mvnw.cmd -Dtest=FileStorageServiceImplTest test
```

Expected: GIF currently passes and fake PNG is not detected, so tests fail for the intended reasons.

- [ ] **Step 2: Implement minimal signature detection**

Add a package-private `ImageType detectImageType(byte[] header)` returning `JPEG`, `PNG`, `WEBP`, or throwing `FileStorageException`. Remove GIF from allowed content types/extensions. Require detected type to match both extension and declared MIME type. Continue generating UUID names and resolving inside normalized roots.

- [ ] **Step 3: Run tests and commit**

```powershell
./mvnw.cmd -Dtest=FileStorageServiceImplTest,SecurityBoundaryTest test
./mvnw.cmd test
git add project-boot/src/main/java/com/zk/projectboot/service/impl/FileStorageServiceImpl.java project-boot/src/main/java/com/zk/projectboot/controller/FileUploadController.java project-boot/src/test/java/com/zk/projectboot/service/FileStorageServiceImplTest.java
git commit -m "feat: validate uploaded image content"
```

Expected: Maven exits 0 and the upload limit is 5 MiB in configuration.

---

### Task 5: Implement Seven-Day Visitor Retention

**Files:**
- Modify: `project-boot/src/main/java/com/zk/projectboot/model/User.java`
- Modify: `project-boot/src/main/java/com/zk/projectboot/application/AuthApplicationService.java`
- Modify: `project-boot/src/main/java/com/zk/projectboot/repository/UserRepository.java`
- Create: `project-boot/src/main/java/com/zk/projectboot/config/TimeConfig.java`
- Create: `project-boot/src/main/java/com/zk/projectboot/retention/VisitorCleanupReport.java`
- Create: `project-boot/src/main/java/com/zk/projectboot/retention/VisitorDataCleanupService.java`
- Create: `project-boot/src/main/java/com/zk/projectboot/retention/VisitorDataCleanupScheduler.java`
- Create: `project-boot/src/main/java/com/zk/projectboot/config/DemoDataInitializer.java`
- Test: `project-boot/src/test/java/com/zk/projectboot/retention/VisitorDataCleanupServiceTest.java`
- Test: `project-boot/src/test/java/com/zk/projectboot/config/DemoDataInitializerTest.java`

**Interfaces:**
- Produces: `VisitorCleanupReport cleanupExpiredVisitors(Instant cutoff)` and permanent demo data created only when `demo.seed.enabled=true`.

- [ ] **Step 1: Write failing retention boundary tests**

Create fixtures at exactly 6d23h59m, 7d, and 7d1m before the injected clock. Assert:

```java
EPHEMERAL at 6d23h59m -> retained
EPHEMERAL at 7d and 7d1m -> deleted
PERMANENT at 30d -> retained
expired user's articles/comments/likes/favorites -> deleted
unexpired user's interactions with published permanent content -> retained
```

Run:

```powershell
./mvnw.cmd -Dtest=VisitorDataCleanupServiceTest test
```

Expected: compilation fails because retention types and service do not exist.

- [ ] **Step 2: Add the explicit retention model**

Add:

```java
@Enumerated(EnumType.STRING)
@Column(name = "retention_policy", nullable = false, length = 16)
private RetentionPolicy retentionPolicy = RetentionPolicy.EPHEMERAL;

public enum RetentionPolicy { PERMANENT, EPHEMERAL }
```

Set `EPHEMERAL` explicitly during public registration. Query expired users with:

```java
List<User> findByRetentionPolicyAndCreatedAtLessThanEqual(
    User.RetentionPolicy retentionPolicy, LocalDateTime cutoff);
```

- [ ] **Step 3: Implement transactional cleanup and post-commit file deletion**

Collect avatar and cover filenames before deleting each expired aggregate. Delete database aggregates transactionally. Publish a cleanup-completed event containing only normalized file names; delete physical files in an `AFTER_COMMIT` listener. Log counts, never user fields.

- [ ] **Step 4: Write and pass demo seed tests**

Tests prove the initializer is disabled by default, creates only `PERMANENT` accounts when enabled, BCrypt-encodes `DEMO_USER_PASSWORD`, and remains idempotent by username. No password hash is embedded in SQL or Java.

- [ ] **Step 5: Schedule and verify**

Enable scheduling on the application and configure:

```java
@Scheduled(cron = "${cleanup.cron:0 30 3 * * *}", zone = "Asia/Shanghai")
@ConditionalOnProperty(name = "cleanup.enabled", havingValue = "true", matchIfMissing = true)
```

Run:

```powershell
./mvnw.cmd -Dtest=VisitorDataCleanupServiceTest,DemoDataInitializerTest test
./mvnw.cmd test
git add project-boot/src/main project-boot/src/test deploy/mysql/01-schema.sql
git commit -m "feat: expire visitor data after seven days"
```

Expected: Maven exits 0; schema baseline includes `retention_policy` and no seeded password hashes.

---

### Task 6: Add Production Containers, Backup, and Nginx Delivery

**Files:**
- Modify: `docker-compose.yml`
- Modify: `project-boot/Dockerfile`
- Modify: `project-vue/Dockerfile`
- Modify: `project-vue/nginx.conf`
- Create: `deploy/nginx/https.conf.template`
- Create: `deploy/backup/backup.sh`
- Create: `deploy/backup/backup.ps1`

**Interfaces:**
- Consumes: schema SQL and environment contract from Tasks 1–2.
- Produces: healthy `mysql`, `backend`, and `frontend` services; `backup.sh BACKUP_DIR KEEP_DB KEEP_UPLOADS`.

- [ ] **Step 1: Write executable configuration assertions before editing**

Run and save the expected failing baseline:

```powershell
docker compose config | Select-String "3306:|8080:"
Select-String -Path docker-compose.yml -Pattern "mem_limit|healthcheck|json-file|MYSQL_APP_USER"
Select-String -Path project-vue/nginx.conf -Pattern "gzip on|Cache-Control|/actuator/health"
```

Expected: required resource, logging, and caching assertions are missing.

- [ ] **Step 2: Implement the bounded Compose topology**

Compose must include:

```yaml
mysql:
  mem_limit: 640m
  expose: ["3306"]
backend:
  mem_limit: 512m
  environment:
    JAVA_TOOL_OPTIONS: -Xms128m -Xmx384m -XX:+UseSerialGC
  expose: ["8080"]
frontend:
  mem_limit: 128m
  ports:
    - "${APP_HTTP_PORT:-80}:80"
```

All services use `json-file` logging with `max-size: 10m` and `max-file: 3`. MySQL creates `${MYSQL_APP_USER}` and `${MYSQL_APP_PASSWORD}`. Only frontend publishes a host port.

- [ ] **Step 3: Implement Nginx caching and proxy policy**

Required blocks:

```nginx
gzip on;
gzip_types text/plain text/css application/javascript application/json image/svg+xml;
location ~* \.(?:js|css|webp|png|jpg|jpeg|svg|woff2)$ {
  expires 1y;
  add_header Cache-Control "public, immutable";
}
location = /index.html {
  add_header Cache-Control "no-cache";
}
location /api/ {
  proxy_pass http://backend:8080;
  proxy_set_header X-Forwarded-Proto $scheme;
  proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
}
location = /health {
  proxy_pass http://backend:8080/actuator/health;
}
```

- [ ] **Step 4: Implement backup scripts**

`backup.sh` must run `mysqldump --single-transaction --routines --triggers`, gzip to a UTC/Asia-Shanghai timestamped filename, archive uploads with the same timestamp, retain seven DB archives and three upload archives, write with `umask 077`, and exit non-zero on any failure. `backup.ps1` mirrors the flow for local validation without embedding credentials.

- [ ] **Step 5: Validate containers and backup, then commit**

```powershell
docker compose config --quiet
docker compose build
docker compose up -d
docker compose ps
Invoke-RestMethod http://localhost/health
docker compose exec -T backend sh -c 'test -d /app/uploads'
docker compose down
git add docker-compose.yml project-boot/Dockerfile project-vue/Dockerfile project-vue/nginx.conf deploy
git commit -m "ops: add bounded production deployment and backups"
```

Expected: config/build commands exit 0, all three services healthy, `/health` reports `UP`, and volumes survive container recreation.

---

### Task 7: Add Tested Frontend Session and Rate-Limit UX

**Files:**
- Modify: `project-vue/package.json`
- Modify: `project-vue/package-lock.json`
- Create: `project-vue/src/api/errorPolicy.js`
- Create: `project-vue/tests/unit/errorPolicy.spec.js`
- Modify: `project-vue/src/api/index.js`
- Modify: `project-vue/src/api/config.js`

**Interfaces:**
- Produces: `classifyApiError(error)` returning `{ action, message }` for 401, 403, 429, and other failures.

- [ ] **Step 1: Add the matching Vue CLI Jest runner**

```powershell
npm install --save-dev @vue/cli-plugin-unit-jest@~5.0.0 jest@^27.5.1
```

Add script: `"test:unit": "vue-cli-service test:unit"`.

- [ ] **Step 2: Write failing policy tests**

```javascript
expect(classifyApiError({ response: { status: 401 } })).toEqual({ action: 'login', message: '登录状态已失效，请重新登录' })
expect(classifyApiError({ response: { status: 403 } }).action).toBe('forbidden')
expect(classifyApiError({ response: { status: 429 } })).toEqual({ action: 'notify', message: '操作过于频繁，请稍后重试' })
```

Run `npm run test:unit -- errorPolicy.spec.js`.

Expected: FAIL because module does not exist.

- [ ] **Step 3: Implement and integrate the minimal policy**

The Axios interceptor must reject the original error after dispatching the selected action. On 401, clear Session-backed store state and redirect to `/login` only when not already there. On 429, preserve the current page and show one notification. Remove request/response debug logging.

- [ ] **Step 4: Remove the production fixed IP and verify**

`src/api/config.js` must default to `/api`; remove `.env.production` or change it to `VUE_APP_API_URL=/api`.

Run:

```powershell
npm run test:unit
npm run build
rg -n "118\.178\.235\.178" src dist .env.production
```

Expected: tests/build exit 0; fixed-IP scan returns no matches.

- [ ] **Step 5: Commit**

```powershell
git add project-vue/package.json project-vue/package-lock.json project-vue/src/api project-vue/tests project-vue/.env.production
git commit -m "feat: handle session expiry and rate limits in Vue"
```

---

### Task 8: Reduce Frontend Image Cost for 3 Mbps Delivery

**Files:**
- Replace: `project-vue/src/assets/default-cover.png` with `project-vue/src/assets/default-cover.webp`
- Modify: `project-vue/src/components/ArticleCard.vue`
- Modify: `project-vue/src/views/Home.vue`
- Modify: `project-vue/src/views/Search.vue`
- Modify: `project-vue/src/views/ArticlesByCategory.vue`
- Modify: `project-vue/src/views/ArticlesByTag.vue`
- Modify: `project-vue/src/views/ArticleDetail.vue`

**Interfaces:**
- Produces: a default cover under 200 KiB and lazy-loaded non-hero list images.

- [ ] **Step 1: Record the failing asset budget**

```powershell
$asset = Get-Item project-vue/src/assets/default-cover.png
if ($asset.Length -le 204800) { throw 'Baseline unexpectedly already meets budget' }
```

Expected: current 2 MiB asset exceeds the 200 KiB budget.

- [ ] **Step 2: Convert using the image compression skill/tooling**

Generate `default-cover.webp` at visually acceptable quality and dimensions no larger than 1600×900. Delete the PNG only after all imports are updated.

- [ ] **Step 3: Add loading and dimensions**

All article list images receive:

```html
loading="lazy"
decoding="async"
width="640"
height="360"
```

The first above-the-fold hero image may use `fetchpriority="high"` and must not use lazy loading.

- [ ] **Step 4: Verify the budget and build**

```powershell
$asset = Get-Item project-vue/src/assets/default-cover.webp
if ($asset.Length -gt 204800) { throw "Default cover exceeds 200 KiB: $($asset.Length)" }
npm run build
rg -n "default-cover\.png" project-vue/src project-vue/dist
```

Expected: asset is at most 204800 bytes, build exits 0, PNG scan has no matches.

- [ ] **Step 5: Commit**

```powershell
git add project-vue/src/assets project-vue/src/components/ArticleCard.vue project-vue/src/views
git commit -m "perf: reduce article image transfer cost"
```

---

### Task 9: Complete Security Characterization and Delivery Documentation

**Files:**
- Modify: `project-boot/src/test/java/com/zk/projectboot/security/SecurityBoundaryTest.java`
- Create: `README.md`
- Rewrite: `DEPLOY.md`
- Create: `BACKUP.md`

**Interfaces:**
- Produces: executable evidence for authorization and an operator-ready runbook.

- [ ] **Step 1: Add missing failing security boundary tests**

Add tests for:

```text
owner reads own pending article -> 200
other user reads pending article -> 404
regular user publishes pending article -> 403
admin publishes pending article -> 200
banned user mutation -> 401 or 403 according to SecurityConfig contract
anonymous upload -> 401
```

Run `./mvnw.cmd -Dtest=SecurityBoundaryTest test` and confirm each newly exposed gap fails for the expected status before changing implementation.

- [ ] **Step 2: Make the minimal security corrections**

Modify only the affected controller/application/security rules. Preserve current API paths and response envelopes. Re-run the focused test after each correction.

- [ ] **Step 3: Write operator-facing documentation**

README must include product positioning, architecture, feature matrix, security properties, local startup, test commands, deployment overview, screenshots path, and a blank online-demo section stating that the URL will be added after ICP/HTTPS completion.

DEPLOY must include Alibaba Cloud security-group ports 22/80/443 only, Ubuntu/Alibaba Cloud Linux Docker installation branches, `.env` generation, `docker compose up -d --build`, health checks, TLS template activation, upgrade/rollback, and acceptance checklist.

BACKUP must include cron entry:

```cron
15 3 * * * cd /opt/blogsys && /opt/blogsys/deploy/backup/backup.sh >> /var/log/blogsys-backup.log 2>&1
```

and exact restore sequencing for matching database/uploads timestamps.

- [ ] **Step 4: Run documentation and security scans**

```powershell
rg -n "TBD|TODO|118\.178\.235\.178|123456|blogsys123|BLOGSESSION=" README.md DEPLOY.md BACKUP.md .env.example docker-compose.yml project-boot project-vue -g '!**/node_modules/**' -g '!**/target/**' -g '!**/dist/**'
./mvnw.cmd test
npm run test:unit
npm run build
```

Expected: no unsafe/placeholder matches; all commands exit 0.

- [ ] **Step 5: Commit**

```powershell
git add README.md DEPLOY.md BACKUP.md project-boot/src project-boot/pom.xml project-vue
git commit -m "docs: prepare BlogSys for public portfolio deployment"
```

---

### Task 10: Run Final Publication Gates and Push GitHub

**Files:**
- Verify all tracked files.
- Update `README.md` only if verification commands changed from the documented commands.

**Interfaces:**
- Produces: public `main` branch at `zk-boop/blogsys-community` with reproducible build evidence.

- [ ] **Step 1: Run the clean backend gate**

```powershell
./mvnw.cmd clean test
./mvnw.cmd -DskipTests package
```

Expected: both commands exit 0 with zero test failures.

- [ ] **Step 2: Run the clean frontend gate**

```powershell
Remove-Item -LiteralPath dist -Recurse -Force -ErrorAction SilentlyContinue
npm ci
npm run test:unit
npm run build
```

Expected: dependency install, tests, and build exit 0.

- [ ] **Step 3: Run the Compose and persistence gate**

```powershell
docker compose config --quiet
docker compose up -d --build
docker compose ps
Invoke-RestMethod http://localhost/health
docker compose restart backend frontend
Invoke-RestMethod http://localhost/health
docker compose down
```

Expected: services are healthy before and after restart.

- [ ] **Step 4: Run final repository publication checks**

```powershell
git status --short
git diff --check HEAD
git ls-files | rg "(^|/)(\.env|node_modules|target|dist|uploads|backups)(/|$)"
rg -n --hidden -g '!**/.git/**' "118\.178\.235\.178|BEGIN (RSA|OPENSSH|EC) PRIVATE KEY|MYSQL_ROOT_PASSWORD=[^r]|BLOGSESSION=" .
git log --oneline --decorate -10
```

Expected: clean status, no ignored/sensitive tracked files, no secret matches, and reviewable task commits.

- [ ] **Step 5: Push the public repository**

```powershell
git push -u origin main
git ls-remote --heads origin main
```

Expected: push succeeds and remote reports `refs/heads/main` at the local HEAD commit.

- [ ] **Step 6: Record the release handoff**

Report the exact backend test count, frontend test count, build status, image size, Compose health state, remote commit hash, remaining ICP/HTTPS actions, and any verification that could not run on the local machine.
