-- liquibase formatted sql
-- 
-- 最终整合版数据库初始化脚本
-- 包含所有表结构的最终状态（已整合所有变更）
-- 注意：此脚本仅供全新部署使用，已有数据库请继续使用增量变更脚本（001-007）
--

-- changeset mulehang:000-init-schema-final splitStatements:true endDelimiter:;

-- ============================================================
-- 用户与权限管理
-- ============================================================

CREATE TABLE `sys_user` (
    `id`               BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`         VARCHAR(64) NOT NULL COMMENT '登录名，唯一',
    `password_hash`    VARCHAR(128) NOT NULL COMMENT '密码哈希',
    `password_salt`    VARCHAR(64) NOT NULL COMMENT '密码盐',
    `nickname`         VARCHAR(64) NOT NULL COMMENT '昵称',
    `email`            VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `mobile`           VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    `avatar`           VARCHAR(255) DEFAULT NULL COMMENT '头像地址',
    `profile`          TEXT COMMENT '个人简介',
    `status`           TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `register_ip`      VARCHAR(64) DEFAULT NULL COMMENT '注册IP',
    `last_login_ip`    VARCHAR(64) DEFAULT NULL COMMENT '最近登录IP',
    `last_login_time`  DATETIME DEFAULT NULL COMMENT '最近登录时间',
    `is_deleted`       TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_username` (`username`),
    UNIQUE KEY `uk_sys_user_email` (`email`),
    KEY `idx_sys_user_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE `sys_role` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `code`        VARCHAR(64) NOT NULL COMMENT '角色编码，唯一',
    `name`        VARCHAR(64) NOT NULL COMMENT '角色名称',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `sort`        INT NOT NULL DEFAULT 100 COMMENT '排序值，越小越靠前',
    `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

CREATE TABLE `sys_user_role` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT UNSIGNED NOT NULL COMMENT '逻辑外键，关联 sys_user.id',
    `role_id`     BIGINT UNSIGNED NOT NULL COMMENT '逻辑外键，关联 sys_role.id',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_sys_user_role` (`user_id`, `role_id`),
    KEY `idx_sys_user_role_user` (`user_id`),
    KEY `idx_sys_user_role_role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户-角色关联表（逻辑外键，无物理约束）';

-- ============================================================
-- 博客内容管理
-- ============================================================

CREATE TABLE `blog_category` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`   BIGINT UNSIGNED DEFAULT 0 COMMENT '逻辑外键，关联 blog_category.id，0 为顶级',
    `name`        VARCHAR(64) NOT NULL COMMENT '分类名称',
    `slug`        VARCHAR(64) NOT NULL COMMENT '分类唯一标识',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `sort`        INT NOT NULL DEFAULT 100 COMMENT '排序值',
    `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `creator_id`  BIGINT UNSIGNED DEFAULT NULL COMMENT '创建者ID，关联 sys_user.id',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_category_slug_deleted` (`slug`, `is_deleted`),
    KEY `idx_blog_category_parent` (`parent_id`),
    KEY `idx_blog_category_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类';

CREATE TABLE `blog_tag` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '标签名称',
    `slug`        VARCHAR(64) NOT NULL COMMENT '标签唯一标识',
    `color`       VARCHAR(32) DEFAULT NULL COMMENT '展示颜色',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `creator_id`  BIGINT UNSIGNED DEFAULT NULL COMMENT '创建者ID，关联 sys_user.id',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_tag_slug_deleted` (`slug`, `is_deleted`),
    UNIQUE KEY `uk_blog_tag_name_deleted` (`name`, `is_deleted`),
    KEY `idx_blog_tag_creator` (`creator_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章标签';

CREATE TABLE `blog_column` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '专栏名称',
    `slug`        VARCHAR(64) NOT NULL COMMENT '专栏唯一标识',
    `cover_url`   VARCHAR(255) DEFAULT NULL COMMENT '封面图',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `sort`        INT NOT NULL DEFAULT 100 COMMENT '排序值',
    `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_column_slug` (`slug`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章专栏/系列';

CREATE TABLE `blog_article` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`          VARCHAR(200) NOT NULL COMMENT '标题',
    `slug`           VARCHAR(200) NOT NULL COMMENT '文章唯一标识，用于路由',
    `summary`        VARCHAR(500) DEFAULT NULL COMMENT '摘要',
    `cover_url`      VARCHAR(255) DEFAULT NULL COMMENT '封面图地址',
    `status`         TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-草稿 1-已发布 2-私密',
    `source_type`    TINYINT NOT NULL DEFAULT 1 COMMENT '文章来源 1-原创 2-转载 3-翻译',
    `allow_comment`  TINYINT NOT NULL DEFAULT 1 COMMENT '是否允许评论 1-是 0-否',
    `is_pinned`      TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 1-是 0-否',
    `author_id`      BIGINT UNSIGNED NOT NULL COMMENT '逻辑外键，关联 sys_user.id',
    `category_id`    BIGINT UNSIGNED DEFAULT NULL COMMENT '逻辑外键，关联 blog_category.id',
    `column_id`      BIGINT UNSIGNED DEFAULT NULL COMMENT '逻辑外键，关联 blog_column.id',
    `word_count`     INT NOT NULL DEFAULT 0 COMMENT '字数统计',
    `read_count`     BIGINT NOT NULL DEFAULT 0 COMMENT '阅读数',
    `like_count`     INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `comment_count`  INT NOT NULL DEFAULT 0 COMMENT '评论数',
    `publish_time`   DATETIME DEFAULT NULL COMMENT '发布时间',
    `is_deleted`     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_article_slug` (`slug`),
    KEY `idx_blog_article_author` (`author_id`),
    KEY `idx_blog_article_category` (`category_id`),
    KEY `idx_blog_article_column` (`column_id`),
    KEY `idx_blog_article_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章主表（逻辑外键，无物理约束）';

CREATE TABLE `blog_article_body` (
    `id`            BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`    BIGINT UNSIGNED NOT NULL COMMENT '逻辑外键，关联 blog_article.id',
    `content_md`    LONGTEXT COMMENT 'Markdown 原文',
    `content_html`  LONGTEXT COMMENT '渲染后的 HTML',
    `is_deleted`    TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_article_body_article` (`article_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章内容表';

CREATE TABLE `blog_article_tag` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`  BIGINT UNSIGNED NOT NULL COMMENT '逻辑外键，关联 blog_article.id',
    `tag_id`      BIGINT UNSIGNED NOT NULL COMMENT '逻辑外键，关联 blog_tag.id',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_article_tag` (`article_id`, `tag_id`),
    KEY `idx_blog_article_tag_article` (`article_id`),
    KEY `idx_blog_article_tag_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章与标签关联表（逻辑外键）';

CREATE TABLE `blog_comment` (
    `id`             BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `article_id`     BIGINT UNSIGNED NOT NULL COMMENT '逻辑外键，关联 blog_article.id',
    `root_id`        BIGINT UNSIGNED DEFAULT 0 COMMENT '根评论ID，0 表示自身为根',
    `parent_id`      BIGINT UNSIGNED DEFAULT 0 COMMENT '父评论ID，0 表示直接评论文章',
    `user_id`        BIGINT UNSIGNED DEFAULT NULL COMMENT '逻辑外键，关联 sys_user.id（未登录访客可为空）',
    `reply_to_user`  BIGINT UNSIGNED DEFAULT NULL COMMENT '逻辑外键，被回复的用户ID',
    `content`        TEXT NOT NULL COMMENT '评论内容',
    `status`         TINYINT NOT NULL DEFAULT 0 COMMENT '状态 0-待审核 1-通过 2-拒绝/屏蔽',
    `like_count`     INT NOT NULL DEFAULT 0 COMMENT '点赞数',
    `ip_address`     VARCHAR(64) DEFAULT NULL COMMENT 'IP 地址',
    `user_agent`     VARCHAR(255) DEFAULT NULL COMMENT 'User-Agent',
    `location`       VARCHAR(128) DEFAULT NULL COMMENT '归属地',
    `is_top`         TINYINT NOT NULL DEFAULT 0 COMMENT '是否置顶 1-是 0-否',
    `is_deleted`     TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_blog_comment_article` (`article_id`),
    KEY `idx_blog_comment_root` (`root_id`),
    KEY `idx_blog_comment_parent` (`parent_id`),
    KEY `idx_blog_comment_user` (`user_id`),
    KEY `idx_blog_comment_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章评论（逻辑外键，无物理约束）';

-- ============================================================
-- 文件管理
-- ============================================================

CREATE TABLE `file_detail` (
    `id`               VARCHAR(32) NOT NULL COMMENT '文件ID',
    `url`              VARCHAR(512) DEFAULT NULL COMMENT '文件访问地址',
    `filename`         VARCHAR(256) DEFAULT NULL COMMENT '文件名称',
    `original_filename` VARCHAR(256) DEFAULT NULL COMMENT '原始文件名',
    `base_path`        VARCHAR(256) DEFAULT NULL COMMENT '基础路径',
    `path`             VARCHAR(256) DEFAULT NULL COMMENT '文件路径',
    `ext`              VARCHAR(32) DEFAULT NULL COMMENT '文件扩展名',
    `content_type`     VARCHAR(128) DEFAULT NULL COMMENT 'MIME类型',
    `platform`         VARCHAR(32) DEFAULT NULL COMMENT '存储平台',
    `size`             BIGINT DEFAULT NULL COMMENT '文件大小，单位字节',
    `th_url`           VARCHAR(512) DEFAULT NULL COMMENT '缩略图访问地址',
    `th_filename`      VARCHAR(256) DEFAULT NULL COMMENT '缩略图文件名',
    `th_size`          BIGINT DEFAULT NULL COMMENT '缩略图大小，单位字节',
    `th_content_type`  VARCHAR(128) DEFAULT NULL COMMENT '缩略图MIME类型',
    `object_id`        VARCHAR(32) DEFAULT NULL COMMENT '关联对象ID',
    `object_type`      VARCHAR(32) DEFAULT NULL COMMENT '关联对象类型',
    `metadata`         TEXT DEFAULT NULL COMMENT '文件元数据(JSON)',
    `user_metadata`    TEXT DEFAULT NULL COMMENT '用户元数据(JSON)',
    `th_metadata`      TEXT DEFAULT NULL COMMENT '缩略图元数据(JSON)',
    `th_user_metadata` TEXT DEFAULT NULL COMMENT '缩略图用户元数据(JSON)',
    `attr`             TEXT DEFAULT NULL COMMENT '附加属性(JSON)',
    `file_acl`         VARCHAR(32) DEFAULT NULL COMMENT '文件ACL',
    `th_file_acl`      VARCHAR(32) DEFAULT NULL COMMENT '缩略图ACL',
    `hash_info`        TEXT DEFAULT NULL COMMENT '哈希信息(JSON)',
    `upload_id`        VARCHAR(128) DEFAULT NULL COMMENT '上传ID(分片上传)',
    `upload_status`    INT DEFAULT NULL COMMENT '上传状态',
    `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_file_detail_url` (`url`(255)),
    KEY `idx_file_detail_object` (`object_id`, `object_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件记录表';

-- ============================================================
-- 系统配置
-- ============================================================

CREATE TABLE `site_config` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key`  VARCHAR(100) NOT NULL COMMENT '配置键，唯一',
    `config_value` TEXT NOT NULL COMMENT '配置值(JSON / 文本)',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_site_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站点配置表';

-- ============================================================
-- 默认角色与管理员
-- ============================================================

-- 插入默认角色
INSERT IGNORE INTO `sys_role` (`code`, `name`, `description`, `sort`, `status`) VALUES
('ADMIN', '管理员', '系统管理员，拥有所有权限', 1, 1),
('USER', '普通用户', '普通注册用户', 10, 1);

-- 插入默认管理员
-- 密码：mulehang（使用 BCrypt + 自定义盐加密）
-- 注意：生产环境应该通过注册接口创建用户，这里仅用于开发测试
-- 盐值：见 password_salt（Base64 字符串）
-- 密码哈希：使用 BCrypt 对 "mulehang" + password_salt 进行加密
-- 生产时请使用sql更新，不要使用默认管理员
INSERT IGNORE INTO `sys_user` (`username`, `password_hash`, `password_salt`, `nickname`, `email`, `status`) VALUES
('admin', '$2a$10$1PUPrLToxeD2Ktfun6373.yOaDJHDlAtDxcGVWUHxpIHa3ILWS6GS', 'ZUc1eFE2bk4ydkIxbUIyZ0Q4Y1I1dlM0', '管理员', '15871935167@163.com', 1);

-- 分配角色
INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.code = 'ADMIN'
WHERE u.username = 'admin';

INSERT IGNORE INTO `sys_user_role` (`user_id`, `role_id`)
SELECT u.id, r.id
FROM `sys_user` u
JOIN `sys_role` r ON r.code = 'USER'
WHERE u.username = 'admin';

-- rollback DROP TABLE IF EXISTS `site_config`;
-- rollback DROP TABLE IF EXISTS `file_detail`;
-- rollback DROP TABLE IF EXISTS `blog_comment`;
-- rollback DROP TABLE IF EXISTS `blog_article_tag`;
-- rollback DROP TABLE IF EXISTS `blog_article_body`;
-- rollback DROP TABLE IF EXISTS `blog_article`;
-- rollback DROP TABLE IF EXISTS `blog_column`;
-- rollback DROP TABLE IF EXISTS `blog_tag`;
-- rollback DROP TABLE IF EXISTS `blog_category`;
-- rollback DROP TABLE IF EXISTS `sys_user_role`;
-- rollback DROP TABLE IF EXISTS `sys_role`;
-- rollback DROP TABLE IF EXISTS `sys_user`;
