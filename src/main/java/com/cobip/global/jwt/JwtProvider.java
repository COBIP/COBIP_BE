package com.cobip.global.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;

@Getter
@Component
public class JwtProvider {

    private final String secretKey;
    private final String issuer;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;
    private final String header;
    private final String prefix;

    public JwtProvider(
        @Value("${jwt.secret-key}") String secretKey,
        @Value("${jwt.issuer}") String issuer,
        @Value("${jwt.access-token-expiration}") long accessTokenExpiration,
        @Value("${jwt.refresh-token-expiration}") long refreshTokenExpiration,
        @Value("${jwt.header}") String header,
        @Value("${jwt.prefix}") String prefix
    ) {
        this.secretKey = secretKey;
        this.issuer = issuer;
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
        this.header = header;
        this.prefix = prefix;
    }

    public String getAuthorizationHeader() {
        return header;
    }

    public String getTokenPrefix() {
        return prefix;
    }

    public String createAccessToken(Long userId, String email) {
        return createToken(userId, email, accessTokenExpiration);
    }

    public String createRefreshToken(Long userId, String email) {
        return createToken(userId, email, refreshTokenExpiration);
    }

    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public String resolveToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(prefix)) {
            return null;
        }
        return authorizationHeader.substring(prefix.length());
    }

    private String createToken(Long userId, String email, long expirationMillis) {
        Instant now = Instant.now();
        return Jwts.builder()
            .issuer(issuer)
            .subject(String.valueOf(userId))
            .claim("email", email)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(expirationMillis)))
            .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
            .verifyWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
