package com.mulehang.blog.storage;

import org.dromara.x.file.storage.core.FileInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 存储服务抽象。
 */
public interface StorageService {

    /**
     * 上传文件并返回存储元数据。
     */
    FileInfo upload(MultipartFile file);
}
