package com.mulehang.blog.mq.message;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 评论通知消息体（MQ 传输对象）。
 *
 * <p>用于“评论发布后通知作者”的异步链路：</p>
 * <ul>
 *     <li>Producer：在评论入库后，把最小必要信息发到 MQ</li>
 *     <li>Consumer：收到消息后再查 DB，组装邮件内容并发送</li>
 * </ul>
 *
 * @author mulehang
 * @since 2026-01-17
 */
public class CommentNotifyMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long articleId;

    private Long commentId;

    private LocalDateTime timestamp;

    /**
     * 创建评论通知消息。
     *
     * @param articleId 文章 ID
     * @param commentId 评论 ID
     * @return {@link CommentNotifyMessage}
     */
    public static CommentNotifyMessage of(Long articleId, Long commentId) {
        CommentNotifyMessage message = new CommentNotifyMessage();
        message.setArticleId(articleId);
        message.setCommentId(commentId);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    public Long getArticleId() {
        return articleId;
    }

    public void setArticleId(Long articleId) {
        this.articleId = articleId;
    }

    public Long getCommentId() {
        return commentId;
    }

    public void setCommentId(Long commentId) {
        this.commentId = commentId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "CommentNotifyMessage{" +
                "articleId=" + articleId +
                ", commentId=" + commentId +
                ", timestamp=" + timestamp +
                '}';
    }
}

