-- liquibase formatted sql

-- changeset mulehang:005-add-creator-id-to-category-and-tag
-- comment: 为 blog_category 和 blog_tag 表添加 creator_id 字段，用于记录创建者，实现基于创建者的权限控制

-- 添加 creator_id 字段到 blog_category 表
ALTER TABLE `blog_category`
    ADD COLUMN `creator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建者ID，关联 sys_user.id' AFTER `status`,
    ADD INDEX `idx_blog_category_creator` (`creator_id`);

-- 添加 creator_id 字段到 blog_tag 表
ALTER TABLE `blog_tag`
    ADD COLUMN `creator_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '创建者ID，关联 sys_user.id' AFTER `description`,
    ADD INDEX `idx_blog_tag_creator` (`creator_id`);

-- rollback ALTER TABLE `blog_category` DROP COLUMN `creator_id`, DROP INDEX `idx_blog_category_creator`;
-- rollback ALTER TABLE `blog_tag` DROP COLUMN `creator_id`, DROP INDEX `idx_blog_tag_creator`;
