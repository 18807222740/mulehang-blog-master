-- liquibase formatted sql

-- changeset mulehang:007-clean-duplicate-deleted-records
-- comment: 清理重复的已删除记录，只保留每个 slug 的最新删除记录

-- 清理 blog_category 表中的重复已删除记录
-- 对于每个 slug，只保留 id 最大（最新）的已删除记录
DELETE t1 FROM blog_category t1
INNER JOIN blog_category t2 
WHERE t1.slug = t2.slug 
  AND t1.is_deleted = 1 
  AND t2.is_deleted = 1
  AND t1.id < t2.id;

-- 清理 blog_tag 表中的重复已删除记录（按 slug）
DELETE t1 FROM blog_tag t1
INNER JOIN blog_tag t2 
WHERE t1.slug = t2.slug 
  AND t1.is_deleted = 1 
  AND t2.is_deleted = 1
  AND t1.id < t2.id;

-- 清理 blog_tag 表中的重复已删除记录（按 name）
DELETE t1 FROM blog_tag t1
INNER JOIN blog_tag t2 
WHERE t1.name = t2.name 
  AND t1.is_deleted = 1 
  AND t2.is_deleted = 1
  AND t1.id < t2.id;

-- rollback 不提供回滚，因为清理的是重复数据
