package com.mulehang.blog.vo;

import lombok.Data;

/**
 * 文件上传响应。
 */
@Data
public class UploadFileVO {

    /**
     * 文件公共 URL。
     */
    private String url;

    /**
     * 存储平台标识。
     */
    private String platform;

    /**
     * 相对存储路径。
     */
    private String path;

    /**
     * 存储后的文件名。
     */
    private String filename;

    /**
     * 原始文件名。
     */
    private String originalFilename;

    /**
     * 文件扩展名。
     */
    private String ext;

    /**
     * 文件大小（字节）。
     */
    private Long size;
}
