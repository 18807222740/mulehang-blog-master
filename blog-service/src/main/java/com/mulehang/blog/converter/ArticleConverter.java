package com.mulehang.blog.converter;

import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.entity.BlogArticleBody;
import com.mulehang.blog.entity.BlogArticleTag;
import com.mulehang.blog.vo.CategoryVO;
import com.mulehang.blog.vo.ColumnVO;
import com.mulehang.blog.vo.ArticleVO;
import com.mulehang.blog.vo.TagVO;
import com.mulehang.blog.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 文章转换器。
 * <p>
 * 约定：
 * <ul>
 *   <li>
 *     创建文章时，前端入参为 {@link ArticleCreateDTO}，后端落库拆分为两张表：
 *     {@link BlogArticle}（基础信息） + {@link BlogArticleBody}（正文内容）。
 *   </li>
 *   <li>
 *     文章-标签关系落到 {@link BlogArticleTag}，通常在文章插入后根据 tagIds 批量写入。
 *   </li>
 *   <li>
 *     {@link ArticleVO} 用于前台展示，里面的 author/category/column/tags 属于聚合信息，
 *     一般由 Service 查询并组装后，再调用本转换器生成最终 VO。
 *   </li>
 * </ul>
 */
@Mapper(componentModel = "spring")
public interface ArticleConverter {

    /**
     * 创建文章：将入参 DTO 转为文章基础表实体。
     * <p>
     * 说明：
     * <ul>
     *   <li>id 由数据库自增生成</li>
     *   <li>authorId 来自当前登录用户，由 Service 补充</li>
     *   <li>统计字段（字数/阅读/点赞/评论）由业务层计算或初始化</li>
     *   <li>publishTime 一般在发布动作时设置（草稿可为空）</li>
     *   <li>isDeleted/createTime/updateTime 由 MyBatis-Plus 自动维护</li>
     * </ul>
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "authorId", ignore = true)
    @Mapping(target = "wordCount", ignore = true)
    @Mapping(target = "readCount", ignore = true)
    @Mapping(target = "likeCount", ignore = true)
    @Mapping(target = "commentCount", ignore = true)
    @Mapping(target = "publishTime", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    BlogArticle toArticleEntity(ArticleCreateDTO dto);

    /**
     * 创建文章：将入参 DTO 转为文章正文实体。
     * <p>
     * 说明：
     * <ul>
     *   <li>articleId 需要先插入文章基础表后才能获得，因此由 Service 补充</li>
     *   <li>contentHtml 通常由 Markdown 渲染器生成，因此由 Service 补充</li>
     *   <li>isDeleted/createTime/updateTime 由 MyBatis-Plus 自动维护</li>
     * </ul>
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "articleId", ignore = true)
    @Mapping(target = "contentHtml", ignore = true)
    @Mapping(target = "isDeleted", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    BlogArticleBody toArticleBodyEntity(ArticleCreateDTO dto);

    /**
     * 创建/更新文章：将 tagId 列表转换为文章-标签关联实体列表。
     * <p>
     * 说明：
     * <ul>
     *   <li>articleId 需在文章插入后由 Service 统一回填（或在更新时直接设置）</li>
     *   <li>会过滤 null、去重</li>
     * </ul>
     */
    @Named("tagIdsToArticleTags")
    default List<BlogArticleTag> tagIdsToArticleTags(List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return Collections.emptyList();
        }
        return tagIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(tagId -> {
                    BlogArticleTag rel = new BlogArticleTag();
                    rel.setTagId(tagId);
                    return rel;
                })
                .collect(Collectors.toList());
    }

    /**
     * 详情页组装：将文章基础信息 + 正文 HTML + 聚合信息组装成 VO。
     * <p>
     * 说明：author/category/column/tags 通常由 Service 分别查询并传入。
     */
    // 多 source 参数时，如果多个参数都存在同名字段（比如 id），MapStruct 会产生歧义，
    // 因此这里显式指定基础字段都从 article 来源。
    @Mapping(target = "id", source = "article.id")
    @Mapping(target = "title", source = "article.title")
    @Mapping(target = "slug", source = "article.slug")
    @Mapping(target = "summary", source = "article.summary")
    @Mapping(target = "coverUrl", source = "article.coverUrl")
    @Mapping(target = "status", source = "article.status")
    @Mapping(target = "sourceType", source = "article.sourceType")
    @Mapping(target = "allowComment", source = "article.allowComment")
    @Mapping(target = "isPinned", source = "article.isPinned")
    @Mapping(target = "wordCount", source = "article.wordCount")
    @Mapping(target = "readCount", source = "article.readCount")
    @Mapping(target = "likeCount", source = "article.likeCount")
    @Mapping(target = "commentCount", source = "article.commentCount")
    @Mapping(target = "publishTime", source = "article.publishTime")
    @Mapping(target = "contentHtml", source = "contentHtml")
    @Mapping(target = "author", source = "author")
    @Mapping(target = "category", source = "category")
    @Mapping(target = "column", source = "column")
    @Mapping(target = "tags", source = "tags")
    ArticleVO toDetailVO(BlogArticle article,
                         String contentHtml,
                         UserVO author,
                         CategoryVO category,
                         ColumnVO column,
                         List<TagVO> tags);

    /**
     * 详情页（简版）：只组装正文 HTML，其它聚合字段由调用方后续 set 进去。
     * <p>
     * 适合你已经有一套独立的“聚合填充”逻辑（比如统一装配 author/category/tags）。
     */
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "column", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "contentHtml", source = "contentHtml")
    ArticleVO toVO(BlogArticle article, String contentHtml);

    /**
     * 列表页：文章基础信息 -> VO（不返回正文内容）。
     * <p>
     * 列表页通常也不需要组装 author/category/column/tags（或按需组装）。
     */
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "column", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "contentHtml", ignore = true)
    ArticleVO toVO(BlogArticle article);

    /**
     * 列表页批量转换。
     */
    List<ArticleVO> toVOList(List<BlogArticle> articles);
}
