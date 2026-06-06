-- liquibase formatted sql

-- changeset mulehang:001-init-schema splitStatements:true endDelimiter:;
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

CREATE TABLE `blog_category` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `parent_id`   BIGINT UNSIGNED DEFAULT 0 COMMENT '逻辑外键，关联 blog_category.id，0 为顶级',
    `name`        VARCHAR(64) NOT NULL COMMENT '分类名称',
    `slug`        VARCHAR(64) NOT NULL COMMENT '分类唯一标识',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `sort`        INT NOT NULL DEFAULT 100 COMMENT '排序值',
    `status`      TINYINT NOT NULL DEFAULT 1 COMMENT '状态 1-启用 0-禁用',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_category_slug` (`slug`),
    KEY `idx_blog_category_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章分类';

CREATE TABLE `blog_tag` (
    `id`          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(64) NOT NULL COMMENT '标签名称',
    `slug`        VARCHAR(64) NOT NULL COMMENT '标签唯一标识',
    `color`       VARCHAR(32) DEFAULT NULL COMMENT '展示颜色',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '描述',
    `is_deleted`  TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除标记 0-否 1-是',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_blog_tag_slug` (`slug`),
    UNIQUE KEY `uk_blog_tag_name` (`name`)
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

-- rollback DROP TABLE IF EXISTS `site_config`;
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
