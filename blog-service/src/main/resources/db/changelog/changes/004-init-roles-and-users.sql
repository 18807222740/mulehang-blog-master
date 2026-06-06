-- liquibase formatted sql

-- changeset mulehang:004-init-roles-and-users splitStatements:true endDelimiter:;

-- 插入默认角色
INSERT IGNORE INTO `sys_role` (`id`, `code`, `name`, `description`, `sort`, `status`) VALUES
(1, 'ADMIN', '管理员', '系统管理员，拥有所有权限', 1, 1),
(2, 'USER', '普通用户', '普通注册用户', 10, 1);

-- 不再插入默认管理员，避免弱口令风险
-- 如需初始化管理员，请在开发环境通过 blog.admin.init-password 或手动创建
-- rollback DELETE FROM `sys_role` WHERE `id` IN (1, 2);
