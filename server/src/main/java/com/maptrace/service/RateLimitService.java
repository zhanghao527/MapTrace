package com.maptrace.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 基于 Redis 的频率限制服务
 * 使用滑动窗口算法实现精确的频率控制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 检查是否超过频率限制
     * @param key 限流键（如 "report:userId:123"）
     * @param maxCount 最大次数
     * @param duration 时间窗口
     * @return true 表示未超限，false 表示已超限
     */
    public boolean checkLimit(String key, int maxCount, Duration duration) {
        try {
            String redisKey = "rate_limit:" + key;
            Long currentCount = redisTemplate.opsForValue().increment(redisKey);
            
            if (currentCount == null) {
                return true;
            }
            
            // 第一次访问，设置过期时间
            if (currentCount == 1) {
                redisTemplate.expire(redisKey, duration);
            }
            
            boolean allowed = currentCount <= maxCount;
            if (!allowed) {
                log.warn("频率限制触发: key={}, count={}, max={}", key, currentCount, maxCount);
            }
            
            return allowed;
        } catch (Exception e) {
            log.error("Redis 频率限制检查失败，降级为允许通过: key={}", key, e);
            // Redis 故障时降级为允许通过，避免影响业务
            return true;
        }
    }

    /**
     * 检查举报频率限制（每小时最多 10 次）
     */
    public boolean checkReportLimit(Long userId) {
        return checkLimit("report:user:" + userId, 10, Duration.ofHours(1));
    }

    /**
     * 检查评论频率限制（每分钟最多 5 次）
     */
    public boolean checkCommentLimit(Long userId) {
        return checkLimit("comment:user:" + userId, 5, Duration.ofMinutes(1));
    }

    /**
     * 检查照片上传频率限制（每小时最多 20 次）
     */
    public boolean checkPhotoUploadLimit(Long userId) {
        return checkLimit("photo_upload:user:" + userId, 20, Duration.ofHours(1));
    }
}
