package com.mulehang.blog.task;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Redisson 延迟队列中的邮件任务。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailTask implements Serializable {

    /**
     * 用户 ID。
     */
    private Long userId;

    /**
     * 收件人邮箱。
     */
    private String to;

    /**
     * 邮件主题。
     */
    private String subject;

    /**
     * 邮件内容。
     */
    private String content;
}
