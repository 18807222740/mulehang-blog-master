package com.mulehang.blog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件记录实体类，用于 X File Storage 的 FileRecorder 实现
 */
@Data
@TableName("file_detail")
public class FileDetail {

    @TableId(type = IdType.ASSIGN_UUID)
    private String id;// 文件ID

    private String url;// 文件访问地址

    private String filename;// 文件名称

    private String originalFilename;// 原始文件名

    private String basePath;// 基础路径

    private String path;// 文件路径

    private String ext;// 文件扩展名

    private String contentType;// MIME类型

    private String platform;// 存储平台

    private Long size;// 文件大小，单位字节

    private String thUrl;// 缩略图访问地址

    private String thFilename;// 缩略图文件名

    private Long thSize;// 缩略图大小，单位字节

    private String thContentType;// 缩略图MIME类型

    private String objectId;// 关联对象ID

    private String objectType;// 关联对象类型

    private String metadata;// 文件元数据(JSON)

    private String userMetadata;// 用户元数据(JSON)

    private String thMetadata;// 缩略图元数据(JSON)

    private String thUserMetadata;// 缩略图用户元数据(JSON)

    private String attr;// 附加属性(JSON)

    private String fileAcl;// 文件ACL

    private String thFileAcl;// 缩略图ACL

    private String hashInfo;// 哈希信息(JSON)

    private String uploadId;// 上传ID(分片上传)

    private Integer uploadStatus;// 上传状态

    private LocalDateTime createTime;// 创建时间
}
