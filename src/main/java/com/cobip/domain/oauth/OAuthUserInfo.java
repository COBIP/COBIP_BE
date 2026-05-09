package com.cobip.domain.oauth;

public record OAuthUserInfo(
    OAuthProvider provider,
    String providerUserId,
    String email,
    String nickname,
    String profileImageUrl
) {
}
