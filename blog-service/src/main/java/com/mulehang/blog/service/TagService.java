package com.mulehang.blog.service;

import com.mulehang.blog.dto.TagDTO;
import com.mulehang.blog.vo.TagVO;

import java.util.List;

/**
 * 标签 Service。
 */
public interface TagService {

    /**
     * 创建标签。
     */
    Long create(TagDTO dto);

    /**
     * 更新标签。
     */
    void update(Long id, TagDTO dto);

    /**
     * 删除标签。
     */
    void delete(Long id);

    /**
     * 根据 ID 获取标签。
     */
    TagVO getById(Long id);

    /**
     * 获取所有标签。
     */
    List<TagVO> listAll();
}
