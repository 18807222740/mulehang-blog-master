package com.mulehang.blog.controller.api.v1;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mulehang.blog.context.UserContext;
import com.mulehang.blog.dto.CommentCreateDTO;
import com.mulehang.blog.dto.CommentUpdateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.model.Result;
import com.mulehang.blog.service.CommentService;
import com.mulehang.blog.vo.CommentVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 评论 REST API。
 *
 * @author mulehang
 * @since 2026-01-17
 */
@RestController
@Tag(name = "评论管理", description = "评论相关接口")
public class CommentController {

    private final CommentService commentService;

    /**
     * 构造函数。
     *
     * @param commentService 评论服务
     */
    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * 发表评论/回复。
     *
     * @param dto     评论创建 DTO
     * @param request HttpServletRequest
     * @return 评论 ID
     */
    @PostMapping("/api/v1/comments")
    @Operation(summary = "发表评论/回复")
    @SentinelResource(value = "comment-create", blockHandler = "createBlockHandler")
    public Result<Long> create(@Valid @RequestBody CommentCreateDTO dto, HttpServletRequest request) {
        String ip = request == null ? null : request.getRemoteAddr();
        String userAgent = request == null ? null : request.getHeader("User-Agent");
        return Result.ok(commentService.create(dto, ip, userAgent));
    }

    /**
     * 评论限流降级处理。
     */
    public Result<Long> createBlockHandler(CommentCreateDTO dto, HttpServletRequest request, BlockException e) {
        return Result.fail("评论过于频繁，请稍后再试");
    }

    /**
     * 按文章分页查询评论列表。
     *
     * @param articleId 文章 ID
     * @param pageNo    页码（从 1 开始）
     * @param pageSize  每页大小
     * @return 分页结果
     */
    @GetMapping("/api/v1/articles/{articleId}/comments")
    @Operation(summary = "按文章分页查询评论列表")
    public Result<PageResult<CommentVO>> listByArticle(@PathVariable Long articleId,
            Long pageNo,
            Long pageSize) {
        return Result.ok(commentService.listByArticle(articleId, pageNo, pageSize));
    }

    /**
     * 点赞评论。
     *
     * @param id 评论 ID
     * @return 点赞结果（true=成功，false=已点赞或未获取到锁）
     */
    @PostMapping("/api/v1/comments/{id}/like")
    @Operation(summary = "点赞评论")
    public Result<Boolean> like(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(commentService.likeComment(userId, id));
    }

    /**
     * 查询用户是否已点赞评论。
     *
     * @param id 评论 ID
     * @return true=已点赞，false=未点赞
     */
    @GetMapping("/api/v1/comments/{id}/like/status")
    @Operation(summary = "查询用户是否已点赞评论")
    public Result<Boolean> getLikeStatus(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.ok(false);
        }
        return Result.ok(commentService.hasLiked(userId, id));
    }

    /**
     * 取消点赞评论。
     *
     * @param id 评论 ID
     * @return 取消结果（true=成功，false=未点赞或未获取到锁）
     */
    @DeleteMapping("/api/v1/comments/{id}/like")
    @Operation(summary = "取消点赞评论")
    public Result<Boolean> unlike(@PathVariable Long id) {
        Long userId = UserContext.getCurrentUserId();
        if (userId == null) {
            return Result.fail("未登录或登录已过期");
        }
        return Result.ok(commentService.unlikeComment(userId, id));
    }

    /**
     * 编辑评论内容。
     *
     * @param id  评论 ID
     * @param dto 评论更新 DTO
     * @return true=更新成功
     */
    @PutMapping("/api/v1/comments/{id}")
    @Operation(summary = "编辑评论")
    public Result<Boolean> update(@PathVariable Long id, @Valid @RequestBody CommentUpdateDTO dto) {
        commentService.update(id, dto);
        return Result.ok(true);
    }

    /**
     * 删除评论。
     *
     * @param id 评论 ID
     * @return true=删除成功
     */
    @DeleteMapping("/api/v1/comments/{id}")
    @Operation(summary = "删除评论")
    public Result<Boolean> delete(@PathVariable Long id) {
        commentService.delete(id);
        return Result.ok(true);
    }
}
