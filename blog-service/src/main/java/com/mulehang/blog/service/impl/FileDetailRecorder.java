package com.mulehang.blog.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.FileDetail;
import com.mulehang.blog.mapper.FileDetailMapper;
import lombok.extern.slf4j.Slf4j;
import org.dromara.x.file.storage.core.FileInfo;
import org.dromara.x.file.storage.core.hash.HashInfo;
import org.dromara.x.file.storage.core.recorder.FileRecorder;
import org.dromara.x.file.storage.core.upload.FilePartInfo;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * X File Storage 的 FileRecorder 实现，用于将文件信息持久化到数据库
 */
@Slf4j
@Service
public class FileDetailRecorder implements FileRecorder {

    private final FileDetailMapper fileDetailMapper;

    public FileDetailRecorder(FileDetailMapper fileDetailMapper) {
        this.fileDetailMapper = fileDetailMapper;
    }

    /**
     * 保存文件记录
     *
     * @param fileInfo 文件信息
     * @return 是否保存成功
     */
    @Override
    public boolean save(FileInfo fileInfo) {
        FileDetail detail = toFileDetail(fileInfo);
        int rows = fileDetailMapper.insert(detail);
        if (rows > 0) {
            fileInfo.setId(detail.getId());
        }
        return rows > 0;
    }

    /**
     * 更新文件记录
     *
     * @param fileInfo 文件信息
     */
    @Override
    public void update(FileInfo fileInfo) {
        FileDetail detail = toFileDetail(fileInfo);
        fileDetailMapper.updateById(detail);
    }

    /**
     * 根据 URL 获取文件记录
     *
     * @param url 文件访问地址
     * @return 文件信息
     */
    @Override
    public FileInfo getByUrl(String url) {
        FileDetail detail = fileDetailMapper.selectOne(
                new LambdaQueryWrapper<FileDetail>().eq(FileDetail::getUrl, url)
        );
        return detail == null ? null : toFileInfo(detail);
    }

    /**
     * 根据 URL 删除文件记录
     *
     * @param url 文件访问地址
     * @return 是否删除成功
     */
    @Override
    public boolean delete(String url) {
        int rows = fileDetailMapper.delete(
                new LambdaQueryWrapper<FileDetail>().eq(FileDetail::getUrl, url)
        );
        return rows > 0;
    }

    /**
     * 保存文件分片信息（暂不支持分片上传）
     *
     * @param filePartInfo 分片信息
     */
    @Override
    public void saveFilePart(FilePartInfo filePartInfo) {
        log.warn("分片上传功能暂未实现");
    }

    /**
     * 删除文件分片信息（暂不支持分片上传）
     *
     * @param uploadId 上传ID
     */
    @Override
    public void deleteFilePartByUploadId(String uploadId) {
        log.warn("分片上传功能暂未实现");
    }

    /**
     * 将 FileInfo 转换为 FileDetail 实体
     */
    private FileDetail toFileDetail(FileInfo info) {
        FileDetail detail = BeanUtil.copyProperties(
                info, FileDetail.class, "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo"
        );
        // JSON 序列化 Map 类型字段
        detail.setMetadata(jsonOrNull(info.getMetadata()));
        detail.setUserMetadata(jsonOrNull(info.getUserMetadata()));
        detail.setThMetadata(jsonOrNull(info.getThMetadata()));
        detail.setThUserMetadata(jsonOrNull(info.getThUserMetadata()));
        detail.setAttr(dictToJson(info.getAttr()));
        // HashInfo 序列化
        if (info.getHashInfo() != null) {
            detail.setHashInfo(JSONUtil.toJsonStr(info.getHashInfo()));
        }
        // ACL 转字符串
        if (info.getFileAcl() != null) {
            detail.setFileAcl(info.getFileAcl().toString());
        }
        if (info.getThFileAcl() != null) {
            detail.setThFileAcl(info.getThFileAcl().toString());
        }
        return detail;
    }

    /**
     * 将 FileDetail 实体转换为 FileInfo
     */
    private FileInfo toFileInfo(FileDetail detail) {
        FileInfo info = BeanUtil.copyProperties(
                detail, FileInfo.class, "metadata", "userMetadata", "thMetadata", "thUserMetadata", "attr", "hashInfo"
        );
        // JSON 反序列化
        info.setMetadata(toMap(detail.getMetadata()));
        info.setUserMetadata(toMap(detail.getUserMetadata()));
        info.setThMetadata(toMap(detail.getThMetadata()));
        info.setThUserMetadata(toMap(detail.getThUserMetadata()));
        info.setAttr(toDict(detail.getAttr()));
        // HashInfo 反序列化
        if (StrUtil.isNotBlank(detail.getHashInfo())) {
            info.setHashInfo(JSONUtil.toBean(detail.getHashInfo(), HashInfo.class));
        }
        // ACL 保持字符串
        info.setFileAcl(detail.getFileAcl());
        info.setThFileAcl(detail.getThFileAcl());
        return info;
    }

    /**
     * Map 转 JSON 字符串
     */
    private String jsonOrNull(Map<String, String> map) {
        return map == null || map.isEmpty() ? null : JSONUtil.toJsonStr(map);
    }

    /**
     * JSON 字符串转 Map
     */
    @SuppressWarnings("unchecked")
    private Map<String, String> toMap(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSONUtil.toBean(json, Map.class);
    }

    /**
     * JSON 字符串转 Dict
     */
    private Dict toDict(String json) {
        if (StrUtil.isBlank(json)) {
            return null;
        }
        return JSONUtil.toBean(json, Dict.class);
    }

    /**
     * Dict 转 JSON 字符串
     */
    private String dictToJson(Dict dict) {
        return dict == null || dict.isEmpty() ? null : JSONUtil.toJsonStr(dict);
    }
}
