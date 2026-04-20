package com.cobip.global.jwt;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
}
