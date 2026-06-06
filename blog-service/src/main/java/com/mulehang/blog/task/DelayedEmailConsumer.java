package com.mulehang.blog.task;

import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Redisson RDelayedQueue 消费者。
 *
 * <p>
 * 从 Redisson RBlockingQueue 中消费邮件任务，并调用 EmailService 发送邮件。
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DelayedEmailConsumer {

    private final RedissonClient redissonClient;
    private final EmailService emailService;

    private ExecutorService executor;
    private volatile boolean running = true;

    /**
     * 启动消费者线程。
     *
     * <p>
     * 在 Bean 初始化后自动启动一个守护线程，持续从 RBlockingQueue 中消费任务。
     * </p>
     */
    @PostConstruct
    public void start() {
        this.executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setName("reliable-email-consumer");
            t.setDaemon(true);
            return t;
        });

        executor.submit(() -> {
            RBlockingQueue<EmailTask> queue = redissonClient.getBlockingQueue(RedisKeys.DELAYED_EMAIL_QUEUE);
            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // poll 方法会阻塞直到有消息可用，超时时间 10 秒
                    EmailTask task = queue.poll(10, TimeUnit.SECONDS);
                    if (task != null) {
                        try {
                            emailService.sendText(task.getTo(), task.getSubject(), task.getContent());
                            log.debug("邮件任务处理成功: to={}", task.getTo());
                        } catch (Exception e) {
                            log.error("邮件发送失败: to={}", task.getTo(), e);
                        }
                    }
                } catch (Exception e) {
                    log.error("延迟邮件消费者异常", e);
                }
            }
        });
    }

    /**
     * 停止消费者线程。
     *
     * <p>
     * 在 Bean 销毁前自动关闭线程池，中断正在执行的任务。
     * </p>
     */
    @PreDestroy
    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }
}
