package com.mulehang.blog.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.entity.BlogComment;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogCommentMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mq.constant.MqConstants;
import com.mulehang.blog.mq.message.CommentNotifyMessage;
import com.mulehang.blog.service.EmailService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

/**
 * 评论通知消息消费者。
 *
 * <p>职责：收到评论通知消息后，查询文章作者邮箱并发送邮件。</p>
 *
 * @author mulehang
 * @since 2026-01-17
 */
@Slf4j
@Component
public class CommentNotifyConsumer {

    private final BlogArticleMapper articleMapper;
    private final BlogCommentMapper commentMapper;
    private final SysUserMapper userMapper;
    private final EmailService emailService;

    /**
     * 构造函数。
     *
     * @param articleMapper 文章 Mapper
     * @param commentMapper 评论 Mapper
     * @param userMapper    用户 Mapper
     * @param emailService  邮件服务
     */
    public CommentNotifyConsumer(BlogArticleMapper articleMapper,
                                 BlogCommentMapper commentMapper,
                                 SysUserMapper userMapper,
                                 EmailService emailService) {
        this.articleMapper = articleMapper;
        this.commentMapper = commentMapper;
        this.userMapper = userMapper;
        this.emailService = emailService;
    }

    /**
     * 处理评论通知消息（手动 ack）。
     *
     * @param message     消息体
     * @param channel     RabbitMQ Channel
     * @param deliveryTag deliveryTag
     */
    @RabbitListener(queues = MqConstants.COMMENT_NOTIFY_QUEUE)
    public void handleNotify(CommentNotifyMessage message,
                             Channel channel,
                             @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {

        log.info("[MQ Consumer] 收到评论通知消息: {}", message);

        try {
            if (message == null || message.getArticleId() == null || message.getCommentId() == null) {
                log.warn("[MQ Consumer] 评论通知消息无效，直接 ack 丢弃: {}", message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            BlogArticle article = articleMapper.selectById(message.getArticleId());
            if (article == null) {
                log.warn("[MQ Consumer] 文章不存在，直接 ack 丢弃: articleId={}", message.getArticleId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            Long authorId = article.getAuthorId();
            if (authorId == null) {
                log.warn("[MQ Consumer] 文章作者为空，直接 ack 丢弃: articleId={}", article.getId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            SysUser author = userMapper.selectById(authorId);
            if (author == null || author.getEmail() == null || author.getEmail().isBlank()) {
                log.warn("[MQ Consumer] 作者邮箱为空，跳过发信并 ack: authorId={}, articleId={}", authorId, article.getId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            BlogComment comment = commentMapper.selectOne(new LambdaQueryWrapper<BlogComment>()
                    .eq(BlogComment::getId, message.getCommentId())
                    .eq(BlogComment::getArticleId, message.getArticleId()));
            if (comment == null) {
                log.warn("[MQ Consumer] 评论不存在，直接 ack 丢弃: commentId={}, articleId={}",
                        message.getCommentId(), message.getArticleId());
                channel.basicAck(deliveryTag, false);
                return;
            }

            String articleTitle = article.getTitle() == null ? "" : article.getTitle();
            String nickname = author.getNickname() == null ? "" : author.getNickname();
            String slug = article.getSlug() == null ? String.valueOf(article.getId()) : article.getSlug();
            String articleUrl = "http://localhost:8080/articles/" + slug;
            String commentContent = comment.getContent() == null ? "" : comment.getContent();

            String subject = "您的文章收到新评论";
            String content = String.format(
                    "您好 %s，您的文章《%s》收到了新评论：\n\n%s\n\n点击查看：%s",
                    nickname,
                    articleTitle,
                    commentContent,
                    articleUrl
            );

            emailService.sendText(author.getEmail(), subject, content);
            log.info("[MQ Consumer] 评论通知邮件发送完成: to={}, articleId={}, commentId={}",
                    author.getEmail(), article.getId(), comment.getId());

            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[MQ Consumer] 评论通知消息处理失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("[MQ Consumer] 消息拒绝失败", ex);
            }
        }
    }
}

