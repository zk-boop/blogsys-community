#!/usr/bin/env sh
set -eu

umask 077

BACKUP_DIR="${BACKUP_DIR:-/opt/blogsys/backups}"
KEEP_DB="${KEEP_DB:-7}"
KEEP_UPLOADS="${KEEP_UPLOADS:-3}"

if [ -f .env ]; then
  set -a
  # shellcheck disable=SC1091
  . ./.env
  set +a
fi

: "${MYSQL_ROOT_PASSWORD:?MYSQL_ROOT_PASSWORD is required}"
: "${MYSQL_DATABASE:=blog_sys}"

case "$KEEP_DB:$KEEP_UPLOADS" in
  *[!0-9:]*|:*) echo "Retention values must be non-negative integers" >&2; exit 2 ;;
esac

timestamp="$(TZ=Asia/Shanghai date '+%Y%m%d-%H%M%S')"
mkdir -p "$BACKUP_DIR"

database_archive="$BACKUP_DIR/blogsys-db-$timestamp.sql.gz"
uploads_archive="$BACKUP_DIR/blogsys-uploads-$timestamp.tar.gz"
database_tmp="$database_archive.tmp"
uploads_tmp="$uploads_archive.tmp"
trap 'rm -f "$database_tmp" "$uploads_tmp"' EXIT HUP INT TERM

docker compose exec -T -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql \
  mysqldump --single-transaction --routines --triggers -uroot "$MYSQL_DATABASE" \
  | gzip -c > "$database_tmp"
test -s "$database_tmp"
mv "$database_tmp" "$database_archive"

docker compose exec -T backend tar -C /app/uploads -czf - . > "$uploads_tmp"
test -s "$uploads_tmp"
mv "$uploads_tmp" "$uploads_archive"

prune_archives() {
  pattern="$1"
  keep="$2"
  index=0
  for archive in $(ls -1t "$BACKUP_DIR"/$pattern 2>/dev/null || true); do
    index=$((index + 1))
    if [ "$index" -gt "$keep" ]; then
      rm -f -- "$archive"
    fi
  done
}

prune_archives 'blogsys-db-*.sql.gz' "$KEEP_DB"
prune_archives 'blogsys-uploads-*.tar.gz' "$KEEP_UPLOADS"

printf 'Created %s and %s\n' "$database_archive" "$uploads_archive"
