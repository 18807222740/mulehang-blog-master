package com.mulehang.blog.service;

import com.mulehang.blog.dto.ColumnDTO;
import com.mulehang.blog.vo.ColumnVO;

import java.util.List;

/**
 * 专栏 Service。
 */
public interface ColumnService {

    /**
     * 创建专栏。
     */
    Long create(ColumnDTO dto);

    /**
     * 更新专栏。
     */
    void update(Long id, ColumnDTO dto);

    /**
     * 删除专栏。
     */
    void delete(Long id);

    /**
     * 根据 ID 获取专栏。
     */
    ColumnVO getById(Long id);

    /**
     * 获取所有专栏。
     */
    List<ColumnVO> listAll();
}
