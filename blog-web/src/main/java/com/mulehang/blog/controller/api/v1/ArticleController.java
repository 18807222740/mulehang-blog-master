package com.mulehang.blog.controller.api.v1;

import com.mulehang.blog.dto.ArticleCreateDTO;
import com.mulehang.blog.dto.ArticleQueryDTO;
import com.mulehang.blog.dto.ArticleSearchDTO;
import com.mulehang.blog.dto.ArticleUpdateDTO;
import com.mulehang.blog.es.ArticleIndexService;
import com.mulehang.blog.es.ArticleSearchService;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.ArticleService;
import com.mulehang.blog.service.DelayedEmailService;
import com.mulehang.blog.service.LikeService;
import com.mulehang.blog.task.EmailTask;
import com.mulehang.blog.vo.ArticleDetailVO;
import com.mulehang.blog.vo.ArticleListVO;
import com.mulehang.blog.vo.ArticlePublicVO;
import com.mulehang.blog.vo.ArticleSearchVO;
import com.mulehang.blog.context.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * 文章 REST API。
 */
@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "文章管理", description = "文章相关接口")
public class ArticleController {

    private final ArticleService articleService;
    private final LikeService likeService;
    private final DelayedEmailService delayedEmailService;

    /**
     * ES 搜索服务（可选）。
     * <p>
     * 说明：ES 属于 Milestone 3 的可选组件，因此使用 {@link ObjectProvider} 做可选注入。
     * 当 ES 未启用时，搜索接口会返回友好错误信息。
     * </p>
     */
    private final ObjectProvider<ArticleSearchService> articleSearchServiceProvider;

    /**
     * ES 索引服务（可选）。
     */
    private final ObjectProvider<ArticleIndexService> articleIndexServiceProvider;

    /**
     * 构造函数（构造器注入）。
     *
     * <p>通过构造器注入依赖，避免字段注入带来的可测试性与可维护性问题。</p>
     */
    public ArticleController(ArticleService articleService,
                             LikeService likeService,
                             DelayedEmailService delayedEmailService,
                             ObjectProvider<ArticleSearchService> articleSearchServiceProvider,
                             ObjectProvider<ArticleIndexService> articleIndexServiceProvider) {
        this.articleService = articleService;
        this.likeService = likeService;
        this.delayedEmailService = delayedEmailService;
        this.articleSearchServiceProvider = articleSearchServiceProvider;
        this.articleIndexServiceProvider = articleIndexServiceProvider;
    }

    /**
     * 创建文章。
     *
     * @param dto 文章创建 DTO
     * @return 文章 ID
     */
    @PostMapping
    @Operation(summary = "创建文章")
    public Result<Long> create(@Valid @RequestBody ArticleCreateDTO dto) {
        return Result.ok(articleService.createArticle(dto));
    }

    /**
     * 更新文章。
     *
     * @param id 文章 ID
     * @param dto 文章更新 DTO
     * @return 操作结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新文章")
    public Result<Void> update(@PathVariable Long id, @RequestBody ArticleUpdateDTO dto) {
        articleService.updateArticle(id, dto);
        return Result.ok();
    }

    /**
     * 发布文章。
     *
     * @param id 文章 ID
     * @return 操作结果
     */
    @PostMapping("/{id}/publish")
    @Operation(summary = "发布文章")
    public Result<Void> publish(@PathVariable Long id) {
        articleService.publishArticle(id);
        return Result.ok();
    }

    /**
     * 获取文章详情。
     *
     * @param id 文章 ID
     * @return 文章详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取文章详情")
    public Result<ArticleDetailVO> getById(@PathVariable Long id) {
        return Result.ok(articleService.getArticleDetail(id));
    }

    /**
     * 通过 slug 获取文章（前台展示）。
     *
     * @param slug 文章 slug
     * @return 文章详情（不含 contentMd）
     */
    @GetMapping("/slug/{slug}")
    @Operation(summary = "通过 slug 获取文章（前台展示）")
    public Result<ArticlePublicVO> getBySlug(@PathVariable String slug) {
        return Result.ok(articleService.getPublicArticleBySlug(slug));
    }

    /**
     * 分页查询文章。
     *
     * @param query 查询条件
     * @return 分页结果
     */
    @GetMapping
    @Operation(summary = "分页查询文章")
    public Result<PageResult<ArticleListVO>> list(ArticleQueryDTO query) {
        return Result.ok(articleService.listArticles(query));
    }

    /**
     * 搜索文章（Elasticsearch）。
     *
     * <p>接口约定：</p>
     * <ul>
     *     <li>GET /api/v1/articles/search</li>
     *     <li>参数：keyword/pageNo/pageSize/categoryId/authorId/tag</li>
     *     <li>返回：{@link PageResult}<{@link ArticleSearchVO}>，包含 title/summary 高亮字段</li>
     * </ul>
     *
     * <p>为什么不直接复用分页查询接口？</p>
     * <ul>
     *     <li>分页查询：主要走 MySQL like/eq 条件。</li>
     *     <li>搜索：走 ES 全文检索 + 高亮，返回结构会多出 highlight 字段。</li>
     * </ul>
     *
     * @param query 搜索条件
     * @return 搜索结果分页
     */
    @GetMapping("/search")
    @Operation(summary = "全文搜索文章（ES）")
    public Result<PageResult<ArticleSearchVO>> search(ArticleSearchDTO query) {
        ArticleSearchService searchService = articleSearchServiceProvider.getIfAvailable();
        if (searchService == null) {
            // ES 未启用：给出明确提示（不抛异常避免出现 500 stacktrace）
            return Result.fail("Elasticsearch 未启用或未配置：请确认已启动 ES 并配置 spring.elasticsearch.uris");
        }
        return Result.ok(searchService.search(query));
    }

    /**
     * 热门文章 TopN。
     *
     * @param topN 返回数量
     * @return 热门文章列表
     */
    @GetMapping("/hot")
    @Operation(summary = "热门文章 TopN")
    public Result<java.util.List<ArticleListVO>> hot(@RequestParam(defaultValue = "10") int topN) {
        return Result.ok(articleService.listHotArticles(topN));
    }

    /**
     * 点赞文章。
     *
     * @param id 文章 ID
     * @return 点赞结果（true=成功，false=已点赞或未获取到锁）
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{id}/like")
    @Operation(summary = "点赞文章")
    public Result<Boolean> like(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(likeService.likeArticle(userId, id));
    }

    /**
     * 查询用户是否已点赞文章。
     *
     * @param id 文章 ID
     * @return true=已点赞，false=未点赞
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{id}/like/status")
    @Operation(summary = "查询用户是否已点赞文章")
    public Result<Boolean> getLikeStatus(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.ok(false);
        }
        return Result.ok(likeService.hasLiked(userId, id));
    }

    /**
     * 取消点赞文章。
     *
     * @param id 文章 ID
     * @return 取消结果（true=成功，false=未点赞或未获取到锁）
     */
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{id}/like")
    @Operation(summary = "取消点赞文章")
    public Result<Boolean> unlike(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(likeService.unlikeArticle(userId, id));
    }

    /**
     * 延迟邮件测试（仅开发/演示，需要管理员权限）。
     *
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param delaySeconds 延迟秒数
     * @return 操作结果
     */
    @PostMapping("/email/test")
    @Operation(summary = "2.6 延迟邮件测试：投递一条延迟任务（仅开发/演示）")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> enqueueEmail(@RequestParam String to,
                                     @RequestParam(defaultValue = "Test") String subject,
                                     @RequestParam(defaultValue = "Hello") String content,
                                     @RequestParam(defaultValue = "5") long delaySeconds) {
        delayedEmailService.enqueue(new EmailTask(null, to, subject, content), java.time.Duration.ofSeconds(delaySeconds));
        return Result.ok();
    }

    /**
     * 删除文章。
     *
     * @param id 文章 ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除文章")
    public Result<Void> delete(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return Result.ok();
    }

    /**
     * 重建文章搜索索引（ES）。
     *
     * <p>用途：</p>
     * <ul>
     *     <li>初始化时同步种子数据到 ES</li>
     *     <li>修复索引数据不一致</li>
     *     <li>索引结构变更后重建</li>
     * </ul>
     *
     * <p>注意：该接口应该仅管理员可用，生产环境请加上权限验证。</p>
     *
     * @return 同步成功的文章数量
     */
    @PostMapping("/rebuild-search-index")
    @Operation(summary = "重建文章搜索索引（ES）", 
               description = "全量同步已发布文章到 Elasticsearch，用于初始化或修复索引")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Integer> rebuildSearchIndex() {
        ArticleIndexService indexService = articleIndexServiceProvider.getIfAvailable();
        if (indexService == null) {
            return Result.fail("索引服务不可用：请确认 Elasticsearch 已启动并配置 spring.elasticsearch.uris");
        }
        int count = indexService.rebuildAllArticlesIndex();
        Result<Integer> result = Result.ok(count);
        result.setMsg("索引重建完成，成功同步 " + count + " 篇文章");
        return result;
    }
}
