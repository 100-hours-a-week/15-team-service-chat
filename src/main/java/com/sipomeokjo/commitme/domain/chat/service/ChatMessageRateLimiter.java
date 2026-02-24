package com.sipomeokjo.commitme.domain.chat.service;

import com.sipomeokjo.commitme.api.exception.BusinessException;
import com.sipomeokjo.commitme.api.response.ErrorCode;
import com.sipomeokjo.commitme.domain.chat.config.ChatRateLimitProperties;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatMessageRateLimiter {

    private final StringRedisTemplate stringRedisTemplate;
    private final ChatRateLimitProperties chatRateLimitProperties;

    public void validateSendMessageRateLimit(Long userId) {
        if (!chatRateLimitProperties.isEnabled() || userId == null) {
            return;
        }

        String key = chatRateLimitProperties.getKeyPrefix() + ":" + userId;
        Duration window = chatRateLimitProperties.getWindow();
        try {
            Long count = stringRedisTemplate.opsForValue().increment(key);
            if (count == null) {
                return;
            }

            if (count == 1L) {
                stringRedisTemplate.expire(key, window);
            } else {
                Long ttlSeconds = stringRedisTemplate.getExpire(key);
                if (ttlSeconds < 0) {
                    stringRedisTemplate.expire(key, window);
                }
            }

            if (count > chatRateLimitProperties.getMaxRequests()) {
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn(
                    "[ChatRateLimit] 제한 검사 실패 userId={} 사유={} (fail-open)",
                    userId,
                    e.getMessage());
        }
    }
}
