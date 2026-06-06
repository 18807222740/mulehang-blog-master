-- liquibase formatted sql

-- changeset mulehang:008-remove-default-admin splitStatements:true endDelimiter:;

-- 删除默认管理员（仅当仍使用默认密码时）
DELETE FROM `sys_user_role`
WHERE `user_id` IN (
    SELECT `id` FROM `sys_user`
    WHERE `username` = 'admin'
      AND `password_hash` = '$2a$10$1PUPrLToxeD2Ktfun6373.yOaDJHDlAtDxcGVWUHxpIHa3ILWS6GS'
);

DELETE FROM `sys_user`
WHERE `username` = 'admin'
  AND `password_hash` = '$2a$10$1PUPrLToxeD2Ktfun6373.yOaDJHDlAtDxcGVWUHxpIHa3ILWS6GS';

