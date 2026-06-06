package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章分类 Mapper 接口
 */
@Mapper
public interface BlogCategoryMapper extends BaseMapper<BlogCategory> {
}
