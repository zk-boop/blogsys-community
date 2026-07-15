-- Upgrade an existing BlogSys database after introducing seven-day visitor retention.
-- Existing accounts predate the public-demo policy and must remain permanent.

SET @has_retention_column := (
    SELECT COUNT(*)
    FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND column_name = 'retention_policy'
);
SET @add_retention_column := IF(
    @has_retention_column = 0,
    'ALTER TABLE users ADD COLUMN retention_policy VARCHAR(16) NULL',
    'SELECT 1'
);
PREPARE add_retention_column_statement FROM @add_retention_column;
EXECUTE add_retention_column_statement;
DEALLOCATE PREPARE add_retention_column_statement;

UPDATE users
SET retention_policy = 'PERMANENT'
WHERE retention_policy IS NULL OR TRIM(retention_policy) = '';

ALTER TABLE users
    MODIFY COLUMN retention_policy VARCHAR(16) NOT NULL DEFAULT 'EPHEMERAL';

SET @has_retention_index := (
    SELECT COUNT(*)
    FROM information_schema.statistics
    WHERE table_schema = DATABASE()
      AND table_name = 'users'
      AND index_name = 'idx_users_retention_created'
);
SET @add_retention_index := IF(
    @has_retention_index = 0,
    'CREATE INDEX idx_users_retention_created ON users (retention_policy, created_at)',
    'SELECT 1'
);
PREPARE add_retention_index_statement FROM @add_retention_index;
EXECUTE add_retention_index_statement;
DEALLOCATE PREPARE add_retention_index_statement;
