-- liquibase formatted sql

-- changeset mulehang:002-add-file-detail-table splitStatements:true endDelimiter:;
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

-- rollback DROP TABLE IF EXISTS `file_detail`;
