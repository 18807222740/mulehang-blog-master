package com.mulehang.blog.storage;

import cn.hutool.core.util.IdUtil;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.FileStorageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * XFileStorage 的 StorageService 实现。
 */
@Slf4j
@Service
public class XFileStorageServiceImpl implements StorageService {

    private final FileStorageService fileStorageService;
    private final StorageProperties properties;

    public XFileStorageServiceImpl(FileStorageService fileStorageService, StorageProperties properties) {
        this.fileStorageService = fileStorageService;
        this.properties = properties;
    }

    /**
     * 上传文件。
     * @param file 文件
     * @return 文件信息
     */
    @Override
    public FileInfo upload(MultipartFile file) {
        validate(file);

        // 使用唯一文件名，避免同名覆盖/冲突
        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);
        String saveFilename = ext == null || ext.isBlank()
                ? IdUtil.fastSimpleUUID()
                : (IdUtil.fastSimpleUUID() + "." + ext);

        // XFileStorage: url = domain + basePath + path + filename
        FileInfo fileInfo = fileStorageService.of(file)
                .setPath(buildUploadPath())
                .setSaveFilename(saveFilename)
                .upload();

        if (fileInfo == null) {
            throw new IllegalStateException("上传失败");
        }
        log.debug("文件上传成功: platform={}, path={}, filename={}, url={}",
                fileInfo.getPlatform(), fileInfo.getPath(), fileInfo.getFilename(), fileInfo.getUrl());
        return fileInfo;
    }

    /**
     * 验证上传的文件。
     * @param file 文件
     */
    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传的文件为空");
        }
        if (file.getSize() > properties.getMaxSizeBytes()) {
            throw new IllegalArgumentException("文件太大");
        }
        String contentType = file.getContentType();
        String allowedPrefix = properties.getAllowedContentTypePrefix();
        if (StringUtils.hasText(allowedPrefix)) {
            if (!StringUtils.hasText(contentType) || !contentType.startsWith(allowedPrefix)) {
                throw new IllegalArgumentException("不支持的文件类型");
            }
        }
    }

    /**
     * 标准化路径。
     * @param path 路径
     * @return 标准化后的路径
     */
    private String normalizePath(String path) {
        if (!StringUtils.hasText(path)) {
            return "";
        }
        String p = path.trim();
        // XFileStorage 示例使用尾部斜杠，保持一致
        if (!p.endsWith("/")) {
            p = p + "/";
        }
        return p;
    }

    /**
     * 生成上传路径。
     * <p>
     * 规则：basePath + yyyy/MM/dd/（可通过配置关闭或修改格式）。
     */
    private String buildUploadPath() {
        String base = normalizePath(properties.getBasePath());
        if (!properties.isEnableDatePath()) {
            return base;
        }

        String pattern = properties.getDatePathPattern();
        if (!StringUtils.hasText(pattern)) {
            pattern = "yyyy/MM/dd";
        }

        String datePart = LocalDate.now().format(DateTimeFormatter.ofPattern(pattern));
        // 兼容 Windows 路径分隔符
        datePart = datePart.replace('\\', '/');
        return normalizePath(base + datePart);
    }
}
