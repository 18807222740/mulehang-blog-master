package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户角色关联
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_user_role")
public class SysUserRole extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private Long userId;// 逻辑外键，关联 sys_user.id

    private Long roleId;// 逻辑外键，关联 sys_role.id
}
