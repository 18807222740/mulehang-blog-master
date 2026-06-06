package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 站点配置
 */
@EqualsAndHashCode(callSuper = true)
@TableName("site_config")
@Data
public class SiteConfig extends BaseEntity {
    @TableId(type = IdType.AUTO)
    private Long id;// 主键

    private String configKey;// 配置键，唯一

    private String configValue;// 配置值(JSON / 文本)

    private String description;// 描述
}
