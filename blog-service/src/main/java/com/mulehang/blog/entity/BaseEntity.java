package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 基础实体类
 */
@Data
public class BaseEntity {

    @TableLogic
    private Integer isDeleted;// 是否删除

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;// 创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;// 更新时间
}
