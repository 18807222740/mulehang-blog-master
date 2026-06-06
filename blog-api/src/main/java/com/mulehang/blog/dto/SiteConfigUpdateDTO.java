package com.mulehang.blog.dto;

import lombok.Data;

/**
 * 站点配置更新 DTO
 */
@Data
public class SiteConfigUpdateDTO {
    private String configValue;// JSON/纯文本
    private String description;// 描述
}
