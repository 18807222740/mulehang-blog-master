package com.mulehang.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.mulehang.blog.entity.BlogArticle;
import com.mulehang.blog.mapper.BlogArticleMapper;
import com.mulehang.blog.redis.RedisKeys;
import com.mulehang.blog.service.SiteStatsService;
import com.mulehang.blog.vo.SiteStatsVO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 网站统计服务实现类
 */
@Service
@RequiredArgsConstructor
public class SiteStatsServiceImpl implements SiteStatsService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final BlogArticleMapper articleMapper;

    /**
     * 记录页面访问。
     * <p>
     * 使用 Redis 记录 PV 和 UV：
     * - PV：每次访问自增计数器
     * - UV：使用 HyperLogLog 记录独立访客
     * </p>
     *
     * @param visitorId 访客唯一标识
     */
    @Override
    public void recordPageView(String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return;
        }

        // 今日 PV +1
        redisTemplate.opsForValue().increment(RedisKeys.SITE_PV_TODAY, 1);

        // 今日 UV（HyperLogLog）
        redisTemplate.opsForHyperLogLog().add(RedisKeys.SITE_UV_TODAY, visitorId);

        // 总 PV +1
        redisTemplate.opsForValue().increment(RedisKeys.SITE_PV_TOTAL, 1);

        // 总 UV（HyperLogLog）
        redisTemplate.opsForHyperLogLog().add(RedisKeys.SITE_UV_TOTAL, visitorId);
    }

    /**
     * 获取网站统计数据。
     *
     * @return 统计数据
     */
    @Override
    public SiteStatsVO getSiteStats() {
        SiteStatsVO vo = new SiteStatsVO();

        // 从 Redis 获取 PV/UV
        Long todayPV = getLongFromRedis(RedisKeys.SITE_PV_TODAY);
        Long totalPV = getLongFromRedis(RedisKeys.SITE_PV_TOTAL);
        Long todayUV = redisTemplate.opsForHyperLogLog().size(RedisKeys.SITE_UV_TODAY);
        Long totalUV = redisTemplate.opsForHyperLogLog().size(RedisKeys.SITE_UV_TOTAL);

        vo.setTodayPV(todayPV != null ? todayPV : 0L);
        vo.setTodayUV(todayUV != null ? todayUV : 0L);
        vo.setTotalPV(totalPV != null ? totalPV : 0L);
        vo.setTotalUV(totalUV != null ? totalUV : 0L);

        // 从数据库统计文章相关数据
        List<BlogArticle> articles = articleMapper.selectList(new LambdaQueryWrapper<BlogArticle>()
                .eq(BlogArticle::getStatus, 1)); // 只统计已发布文章

        vo.setTotalArticles((long) articles.size());
        vo.setTotalReads(articles.stream().mapToLong(a -> a.getReadCount() != null ? a.getReadCount() : 0L).sum());
        vo.setTotalLikes(articles.stream().mapToLong(a -> a.getLikeCount() != null ? a.getLikeCount() : 0L).sum());
        vo.setTotalComments(articles.stream().mapToLong(a -> a.getCommentCount() != null ? a.getCommentCount() : 0L).sum());

        return vo;
    }

    /**
     * 每日 0 点重置今日统计。
     */
    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    public void resetDailyStats() {
        redisTemplate.delete(RedisKeys.SITE_PV_TODAY);
        redisTemplate.delete(RedisKeys.SITE_UV_TODAY);
    }

    /**
     * 从 Redis 获取 Long 值。
     *
     * @param key Redis key
     * @return Long 值
     */
    private Long getLongFromRedis(String key) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return 0L;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
