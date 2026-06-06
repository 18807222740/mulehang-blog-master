-- liquibase formatted sql

-- changeset mulehang:003-seed-test-data splitStatements:true endDelimiter:;
INSERT IGNORE INTO `sys_user`
(`id`, `username`, `password_hash`, `password_salt`, `nickname`, `email`, `status`)
VALUES
(1, 'seed_author', 'test_hash', 'test_salt', 'Seed Author', 'seed@example.com', 1);

INSERT IGNORE INTO `blog_category`
(`id`, `parent_id`, `name`, `slug`, `description`, `sort`, `status`)
VALUES
(1, 0, 'Backend', 'backend', 'Backend articles', 100, 1),
(2, 0, 'Frontend', 'frontend', 'Frontend articles', 100, 1);

INSERT IGNORE INTO `blog_tag`
(`id`, `name`, `slug`, `color`, `description`)
VALUES
(1, 'Spring Boot', 'spring-boot', '#6db33f', 'Spring Boot tag'),
(2, 'Caching', 'caching', '#f59e0b', 'Caching tag'),
(3, 'Observability', 'observability', '#0ea5e9', 'Observability tag');

INSERT IGNORE INTO `blog_column`
(`id`, `name`, `slug`, `cover_url`, `description`, `sort`, `status`)
VALUES
(1, 'Engineering Notes', 'engineering-notes', '', 'Engineering notes column', 100, 1);

INSERT IGNORE INTO `blog_article`
(`id`, `title`, `slug`, `summary`, `status`, `source_type`, `allow_comment`, `is_pinned`, `author_id`, `category_id`, `column_id`, `publish_time`)
VALUES
(1, 'Hello Blog', 'hello-blog', 'First seeded article', 1, 1, 1, 0, 1, 1, 1, NOW()),
(2, 'Caching Basics', 'caching-basics', 'Cache-aside and multi-level cache', 1, 1, 1, 0, 1, 1, 1, NOW()),
(3, 'Observability with Micrometer', 'observability-micrometer', 'Expose cache metrics with Micrometer', 1, 1, 1, 0, 1, 1, 1, NOW());

INSERT IGNORE INTO `blog_article_body`
(`article_id`, `content_md`, `content_html`)
VALUES
(1, '# Hello Blog\n\nThis is the first seeded article.', '<h1>Hello Blog</h1><p>This is the first seeded article.</p>'),
(2, '# Caching Basics\n\nCache-aside + multi-level cache.', '<h1>Caching Basics</h1><p>Cache-aside + multi-level cache.</p>'),
(3, '# Observability with Micrometer\n\nCache metrics via Micrometer.', '<h1>Observability with Micrometer</h1><p>Cache metrics via Micrometer.</p>');

INSERT IGNORE INTO `blog_article_tag`
(`article_id`, `tag_id`)
VALUES
(1, 1),
(2, 2),
(3, 3);

-- rollback DELETE FROM `blog_article_tag` WHERE `article_id` IN (1, 2, 3);
-- rollback DELETE FROM `blog_article_body` WHERE `article_id` IN (1, 2, 3);
-- rollback DELETE FROM `blog_article` WHERE `id` IN (1, 2, 3);
-- rollback DELETE FROM `blog_column` WHERE `id` IN (1);
-- rollback DELETE FROM `blog_tag` WHERE `id` IN (1, 2, 3);
-- rollback DELETE FROM `blog_category` WHERE `id` IN (1, 2);
-- rollback DELETE FROM `sys_user` WHERE `id` IN (1);
