package com.mulehang.blog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 评论更新 DTO
 */
@Data
public class CommentUpdateDTO {

    /** 评论内容 */
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过2000字")
    private String content;
}
