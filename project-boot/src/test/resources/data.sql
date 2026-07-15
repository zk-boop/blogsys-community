INSERT INTO roles (id, role_name, role_description, permissions, created_at, updated_at) VALUES
  (1, 'ROLE_ADMIN', 'System administrator', '["all"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'ROLE_USER', 'Regular user', '["comment","like","favorite"]', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO users (id, username, email, password_hash, nickname, avatar, bio, role_id, status, retention_policy, last_login, created_at, updated_at) VALUES
  (1, 'admin', 'admin@blog.com', '$2b$10$cEEQFzuENWeu.lNXGOQ7n.J/o75oSMesgjmIZglZGs1u10oZKRIOG', 'Administrator', NULL, NULL, 1, 'active', 'PERMANENT', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'lisi', 'lisi@blog.com', '$2b$10$cEEQFzuENWeu.lNXGOQ7n.J/o75oSMesgjmIZglZGs1u10oZKRIOG', 'Li Si', NULL, NULL, 2, 'active', 'PERMANENT', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 'wangwu', 'wangwu@blog.com', '$2b$10$cEEQFzuENWeu.lNXGOQ7n.J/o75oSMesgjmIZglZGs1u10oZKRIOG', 'Wang Wu', NULL, NULL, 2, 'active', 'PERMANENT', NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO categories (id, name, description, parent_id, sort_order, status, created_at, updated_at) VALUES
  (1, '技术', 'Technology', NULL, 1, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, '前端开发', 'Frontend', 1, 1, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, '后端开发', 'Backend', 1, 2, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, '数据库', 'Database', 1, 3, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, '生活', 'Life', NULL, 2, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, '随笔', 'Essay', 5, 1, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, '工具', 'Tools', NULL, 3, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (8, '其他', 'Other', NULL, 99, '1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tags (id, name, description, color, status, created_at, updated_at) VALUES
  (1, 'Java', 'Java', '#b07219', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'Spring', 'Spring', '#6db33f', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 'Vue', 'Vue', '#42b883', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 'React', 'React', '#61dafb', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 'JavaScript', 'JavaScript', '#f7df1e', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, 'MySQL', 'MySQL', '#4479a1', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (7, 'CSS', 'CSS', '#264de4', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (8, 'HTML', 'HTML', '#e34f26', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (9, 'Docker', 'Docker', '#2496ed', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (10, 'Linux', 'Linux', '#fcc624', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO articles (id, title, slug, content, excerpt, cover_image, author_id, category_id, status, view_count, like_count, comment_count, is_featured, is_top, published_at, created_at, updated_at) VALUES
  (2, 'React Hooks 使用指南', 'react-hooks-guide', 'React Hooks content', 'React Hooks introduction', NULL, 3, 2, 'published', 10, 0, 0, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 'Vue 组件设计', 'vue-component-design', 'Vue component content', 'Vue components', NULL, 3, 2, 'published', 5, 1, 0, FALSE, FALSE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, '其他用户草稿', 'other-user-draft', 'Draft content', 'Draft', NULL, 4, 3, 'draft', 0, 0, 0, FALSE, FALSE, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO article_likes (id, article_id, user_id, created_at) VALUES
  (1, 4, 3, CURRENT_TIMESTAMP);

ALTER TABLE roles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE users ALTER COLUMN id RESTART WITH 100;
ALTER TABLE categories ALTER COLUMN id RESTART WITH 100;
ALTER TABLE tags ALTER COLUMN id RESTART WITH 100;
ALTER TABLE articles ALTER COLUMN id RESTART WITH 100;
ALTER TABLE article_likes ALTER COLUMN id RESTART WITH 100;
