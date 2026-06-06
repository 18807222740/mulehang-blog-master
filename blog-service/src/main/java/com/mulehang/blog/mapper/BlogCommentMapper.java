package com.mulehang.blog.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mulehang.blog.entity.BlogComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 文章评论 Mapper 接口
 */
@Mapper
public interface BlogCommentMapper extends BaseMapper<BlogComment> {

    /**
     * 点赞数 +1。
     *
     * @param commentId 评论 ID
     * @return 更新行数
     */
    @Update("UPDATE blog_comment SET like_count = like_count + 1 WHERE id = #{commentId}")
    int incrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 点赞数 -1。
     *
     * @param commentId 评论 ID
     * @return 更新行数
     */
    @Update("UPDATE blog_comment SET like_count = like_count - 1 WHERE id = #{commentId} AND like_count > 0")
    int decrementLikeCount(@Param("commentId") Long commentId);

    /**
     * 统计用户已发布评论数（已通过）。
     *
     * @param userId 用户 ID
     * @return 评论数量
     */
    @Select("SELECT COUNT(*) FROM blog_comment WHERE user_id = #{userId} AND status = 1 AND is_deleted = 0")
    Long countApprovedByUser(@Param("userId") Long userId);
}
