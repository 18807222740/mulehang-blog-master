package com.mulehang.blog.service;

import com.mulehang.blog.dto.CategoryDTO;
import com.mulehang.blog.vo.CategoryVO;

import java.util.List;

/**
 * 分类 Service。
 */
public interface CategoryService {

    /**
     * 创建分类。
     */
    Long create(CategoryDTO dto);

    /**
     * 更新分类。
     */
    void update(Long id, CategoryDTO dto);

    /**
     * 删除分类。
     */
    void delete(Long id);

    /**
     * 根据 ID 获取分类。
     */
    CategoryVO getById(Long id);

    /**
     * 获取所有分类。
     */
    List<CategoryVO> listAll();
}
