package com.cobip.infra.redis;

import java.time.Duration;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RedisService {

    private static final String REFRESH_TOKEN_PREFIX = "refresh-token:";

    private final StringRedisTemplate stringRedisTemplate;

    public void saveRefreshToken(Long userId, String refreshToken, long expirationMillis) {
        stringRedisTemplate.opsForValue()
                .set(refreshTokenKey(userId), refreshToken, Duration.ofMillis(expirationMillis));
    }

    public boolean matchesRefreshToken(Long userId, String refreshToken) {
        String storedToken = stringRedisTemplate.opsForValue().get(refreshTokenKey(userId));
        return refreshToken.equals(storedToken);
    }

    public void deleteRefreshToken(Long userId) {
        stringRedisTemplate.delete(refreshTokenKey(userId));
    }

    private String refreshTokenKey(Long userId) {
        return REFRESH_TOKEN_PREFIX + userId;
    }
}
