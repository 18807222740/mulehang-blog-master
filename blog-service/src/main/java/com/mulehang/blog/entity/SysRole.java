package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("sys_role")
public class SysRole extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private String code;// 角色编码

    private String name;// 角色名称

    private String description;// 描述

    private Integer sort;// 排序值

    private Integer status;// 状态
}
