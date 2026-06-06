package com.mulehang.blog.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.entity.BlogComment;
import com.mulehang.blog.entity.SysUser;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.mapper.BlogCommentMapper;
import com.mulehang.blog.mapper.SysUserMapper;
import com.mulehang.blog.mq.message.CommentNotifyMessage;
import com.mulehang.blog.service.EmailService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CommentNotifyConsumerTest {

    @Test
    void handleNotify_shouldSendEmailAndAck() throws Exception {
        BlogArticleMapper articleMapper = mock(BlogArticleMapper.class);
        BlogCommentMapper commentMapper = mock(BlogCommentMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        EmailService emailService = mock(EmailService.class);
        Channel channel = mock(Channel.class);

        CommentNotifyConsumer consumer = new CommentNotifyConsumer(articleMapper, commentMapper, userMapper,
                emailService);

        BlogArticle article = new BlogArticle();
        article.setId(10L);
        article.setAuthorId(100L);
        article.setTitle("T");
        article.setSlug("slug");
        when(articleMapper.selectById(10L)).thenReturn(article);

        SysUser author = new SysUser();
        author.setId(100L);
        author.setEmail("a@b.com");
        author.setNickname("N");
        when(userMapper.selectById(100L)).thenReturn(author);

        BlogComment comment = new BlogComment();
        comment.setId(20L);
        comment.setArticleId(10L);
        comment.setContent("C");
        when(commentMapper.selectOne(ArgumentMatchers.<LambdaQueryWrapper<BlogComment>>any())).thenReturn(comment);

        CommentNotifyMessage message = CommentNotifyMessage.of(10L, 20L);
        consumer.handleNotify(message, channel, 1L);

        ArgumentCaptor<String> toCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> subjectCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendText(toCaptor.capture(), subjectCaptor.capture(), contentCaptor.capture());
        verify(channel).basicAck(1L, false);

        assertEquals("a@b.com", toCaptor.getValue());
        assertEquals("您的文章收到新评论", subjectCaptor.getValue());
        assertTrue(contentCaptor.getValue().contains("T"));
    }
}
