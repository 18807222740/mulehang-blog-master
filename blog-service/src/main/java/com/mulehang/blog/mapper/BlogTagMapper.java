package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章标签 Mapper 接口
 */
@Mapper
public interface BlogTagMapper extends BaseMapper<BlogTag> {
}
