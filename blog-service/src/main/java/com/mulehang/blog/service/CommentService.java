package com.mulehang.blog.service;

import com.mulehang.blog.dto.CommentCreateDTO;
import com.mulehang.blog.dto.CommentUpdateDTO;
import com.mulehang.blog.model.PageResult;
import com.mulehang.blog.vo.CommentVO;

/**
 * 评论 Service。
 *
 * @author mulehang
 * @since 2026-01-17
 */
public interface CommentService {

    /**
     * 发表评论/回复。
     *
     * @param dto       评论创建 DTO
     * @param ipAddress IP 地址
     * @param userAgent User-Agent
     * @return 评论 ID
     */
    Long create(CommentCreateDTO dto, String ipAddress, String userAgent);

    /**
     * 按文章分页查询评论列表。
     *
     * @param articleId 文章 ID
     * @param pageNo    页码（从 1 开始）
     * @param pageSize  每页大小
     * @return 分页结果
     */
    PageResult<CommentVO> listByArticle(Long articleId, Long pageNo, Long pageSize);

    /**
     * 按用户分页查询评论列表。
     *
     * @param userId   用户 ID
     * @param pageNo   页码（从 1 开始）
     * @param pageSize 每页大小
     * @return 分页结果
     */
    PageResult<CommentVO> listByUser(Long userId, Long pageNo, Long pageSize);

    /**
     * 点赞评论。
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return true=点赞成功；false=已点赞或未获取到锁
     */
    boolean likeComment(Long userId, Long commentId);

    /**
     * 查询用户是否已点赞评论。
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return true=已点赞；false=未点赞
     */
    boolean hasLiked(Long userId, Long commentId);

    /**
     * 取消点赞评论。
     *
     * @param userId    用户 ID
     * @param commentId 评论 ID
     * @return true=取消成功；false=未点赞或未获取到锁
     */
    boolean unlikeComment(Long userId, Long commentId);

    /**
     * 编辑评论内容（仅作者本人）。
     *
     * @param commentId 评论 ID
     * @param dto       评论更新 DTO
     */
    void update(Long commentId, CommentUpdateDTO dto);

    /**
     * 删除评论（仅作者本人）。
     *
     * @param commentId 评论 ID
     */
    void delete(Long commentId);
}
