-- liquibase formatted sql

-- changeset mulehang:006-fix-unique-key-with-is-deleted
-- comment: 修复唯一键约束，使其包含 is_deleted 字段，支持逻辑删除后重复创建

-- 修改 blog_category 表的唯一键约束
ALTER TABLE `blog_category` 
    DROP INDEX `uk_blog_category_slug`,
    ADD UNIQUE KEY `uk_blog_category_slug_deleted` (`slug`, `is_deleted`);

-- 修改 blog_tag 表的唯一键约束
ALTER TABLE `blog_tag` 
    DROP INDEX `uk_blog_tag_slug`,
    DROP INDEX `uk_blog_tag_name`,
    ADD UNIQUE KEY `uk_blog_tag_slug_deleted` (`slug`, `is_deleted`),
    ADD UNIQUE KEY `uk_blog_tag_name_deleted` (`name`, `is_deleted`);

-- rollback ALTER TABLE `blog_tag` DROP INDEX `uk_blog_tag_name_deleted`, DROP INDEX `uk_blog_tag_slug_deleted`, ADD UNIQUE KEY `uk_blog_tag_name` (`name`), ADD UNIQUE KEY `uk_blog_tag_slug` (`slug`);
-- rollback ALTER TABLE `blog_category` DROP INDEX `uk_blog_category_slug_deleted`, ADD UNIQUE KEY `uk_blog_category_slug` (`slug`);
