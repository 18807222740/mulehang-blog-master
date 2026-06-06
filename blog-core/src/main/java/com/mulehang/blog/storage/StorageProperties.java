package com.mulehang.blog.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储相关的业务配置（与 XFileStorage 的底层配置分离）。
 */
@Data
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {

    /**
     * 上传后保存的基础路径（相对路径），例如："upload/"。
     */
    private String basePath = "upload/";

    /**
     * 是否在基础路径下按日期分目录存放。
     * <p>
     * 示例：basePath=upload/ 且 enableDatePath=true，则最终路径类似：upload/2026/01/11/
     */
    private boolean enableDatePath = true;

    /**
     * 日期目录格式，默认 yyyy/MM/dd。
     */
    private String datePathPattern = "yyyy/MM/dd";

    /**
     * 允许上传的最大大小（字节）。默认 20MB。
     */
    private long maxSizeBytes = 20L * 1024 * 1024;

    /**
     * 允许上传的 mime 前缀，默认只允许 image/*。
     */
    private String allowedContentTypePrefix = "image/";
}
