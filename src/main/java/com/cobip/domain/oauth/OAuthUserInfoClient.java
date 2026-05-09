package com.cobip.domain.oauth;

public interface OAuthUserInfoClient {

    OAuthUserInfo getUserInfo(OAuthProvider provider, String accessToken);
}
