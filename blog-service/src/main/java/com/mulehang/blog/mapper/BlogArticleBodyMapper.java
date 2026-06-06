package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogArticleBody;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文章内容 Mapper 接口
 */
@Mapper
public interface BlogArticleBodyMapper extends BaseMapper<BlogArticleBody> {
}
