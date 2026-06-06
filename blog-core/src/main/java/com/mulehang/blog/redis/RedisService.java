package com.mulehang.blog.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Set;

/**
 * Redis 服务。
 */
@Component
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate stringRedisTemplate;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 设置字符串类型的键值对。
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     */
    public void set(String key, String value, Duration timeout) {
        stringRedisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * 获取字符串类型的值。
     * @param key 键
     * @return 值
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * 设置对象类型的键值对。
     * @param key 键
     * @param value 值
     * @param timeout 过期时间
     */
    public void setObject(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
    }

    /**
     * 获取对象类型的值。
     * @param key 键
     * @return 值
     */
    public Object getObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * 设置哈希表中的字段值。
     * @param key 键
     * @param field 字段
     * @param value 值
     */
    public void hSet(String key, String field, Object value) {
        redisTemplate.opsForHash().put(key, field, value);
    }

    /**
     * 获取哈希表中的字段值。
     * @param key 键
     * @param field 字段
     * @return 值
     */
    public Object hGet(String key, String field) {
        return redisTemplate.opsForHash().get(key, field);
    }

    /**
     * 对有序集合中的成员的分数进行递增。
     * @param key 键
     * @param member 成员
     * @param score 分数
     * @return 新的分数
     */
    public Double zIncrBy(String key, String member, double score) {
        return stringRedisTemplate.opsForZSet().incrementScore(key, member, score);
    }

    /**
     * 获取有序集合中指定范围内的元素（按分数降序排列）。
     * @param key 键
     * @param start 起始索引
     * @param end 结束索引
     * @return 元素集合
     */
    public Set<ZSetOperations.TypedTuple<String>> zRevRangeWithScores(String key, long start, long end) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, start, end);
    }

    /**
     * 对指定键的值进行自增操作。
     * @param key 键
     * @return 自增后的值
     */
    public Long increment(String key) {
        return stringRedisTemplate.opsForValue().increment(key);
    }

    /**
     * 对指定键的值进行自增操作。
     * @param key 键
     * @param delta 增量
     * @return 自增后的值
     */
    public Long increment(String key, long delta) {
        return stringRedisTemplate.opsForValue().increment(key, delta);
    }

    /**
     * 删除指定键。
     * @param key 键
     * @return 是否删除成功
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
}
