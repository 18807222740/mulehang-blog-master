package com.mulehang.blog.service.impl;

import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.DelayedEmailService;
import com.mulehang.blog.task.EmailTask;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/*基于 Redisson RDelayedQueue 实现邮件任务的延迟投递。*延迟邮件 Service。**<p>*基于 Redisson RReliableQueue 实现邮件任务的延迟投递。*</p>**<p>*<b>实现说明</b>：*</p>*<p>*Redisson 3.50+将{@code RDelayedQueue} 标记为弃用（参见 GitHub issues #3020, #2998,
 * #1057）。但 {@code RReliableQueue} 为 Pro 功能，本项目使用社区版可用的
 * {@code RDelayedQueue + RBlockingQueue} 组合实现延迟投递。
 * </p>
 */
@Service
@RequiredArgsConstructor
public class DelayedEmailServiceImpl implements DelayedEmailService {

    private final RedissonClient redissonClient;

    /**
     * 投递一个延迟执行的邮件任务。
     *
     * <p>
     * 将邮件任务放入 Redisson RDelayedQueue，到期后任务可被消费者消费。
     * </p>
     * <p>
     * 实现原理：RDelayedQueue 使用 Redis ZSet 保存延迟消息，
     * 到期后转发到 RBlockingQueue，消费者从阻塞队列读取。
     * </p>
     *
     * @param task  邮件任务
     * @param delay 延迟时间，不能为空或负数
     * @throws IllegalArgumentException 当 task 为空或 delay 为空/负数时抛出
     */
    @Override
    @SuppressWarnings("deprecation") // RDelayedQueue 已弃用，但 RReliableQueue 为 Pro 功能，社区版继续使用
    public void enqueue(EmailTask task, Duration delay) {
        if (task == null) {
            throw new IllegalArgumentException("参数 task 不能为空");
        }
        if (delay == null || delay.isNegative()) {
            throw new IllegalArgumentException("参数 delay 不能为空或负数");
        }

        // 获取 RBlockingQueue 与 RDelayedQueue 实例
        RBlockingQueue<EmailTask> blockingQueue = redissonClient.getBlockingQueue(RedisKeys.DELAYED_EMAIL_QUEUE);
        RDelayedQueue<EmailTask> delayedQueue = redissonClient.getDelayedQueue(blockingQueue);
        // 投递任务，指定延迟时间
        delayedQueue.offerAsync(task, delay.toMillis(), TimeUnit.MILLISECONDS);
    }
}
