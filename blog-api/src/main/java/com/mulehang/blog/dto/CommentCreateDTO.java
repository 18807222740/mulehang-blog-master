package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论创建 DTO
 */
@Data
public class CommentCreateDTO {

    /** 文章ID */
    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    /** 父评论ID */
    private Long parentId;

    /** 根评论ID */
    private Long rootId;

    /** 被回复的用户ID */
    private Long replyToUser;

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过2000字")
    private String content;
}
