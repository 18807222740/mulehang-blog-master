package com.mulehang.blog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * WebSocket 通知消息 DTO
 * 用于前后端 WebSocket 消息传输
 *
 * @author mulehang
 * @date 2026-01-21
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {

    /**
     * 通知类型
     * COMMENT: 评论通知
     * REPLY: 回复通知
     * LIKE: 点赞通知
     * SYSTEM: 系统通知
     */
    private String type;

    /**
     * 通知标题
     */
    private String title;

    /**
     * 通知内容
     */
    private String content;

    /**
     * 关联的文章ID
     */
    private Long articleId;

    /**
     * 关联的文章标题
     */
    private String articleTitle;

    /**
     * 评论ID（如果是评论相关通知）
     */
    private Long commentId;

    /**
     * 发送者用户ID
     */
    private Long senderId;

    /**
     * 发送者用户名
     */
    private String senderName;

    /**
     * 接收者用户ID
     */
    private Long receiverId;

    /**
     * 跳转链接
     */
    private String url;

    /**
     * 通知时间
     */
    private LocalDateTime timestamp;

    /**
     * 是否已读
     */
    private Boolean read;
}
