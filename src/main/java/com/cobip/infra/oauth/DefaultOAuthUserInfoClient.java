package com.cobip.infra.oauth;

import com.cobip.domain.oauth.OAuthProvider;
import com.cobip.domain.oauth.OAuthUserInfo;
import com.cobip.domain.oauth.OAuthUserInfoClient;
import com.cobip.global.exception.CustomException;
import com.cobip.global.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class DefaultOAuthUserInfoClient implements OAuthUserInfoClient {

    private final RestClient restClient;
    private final String kakaoUserInfoUri;
    private final String naverUserInfoUri;
    private final String googleUserInfoUri;

    public DefaultOAuthUserInfoClient(
        RestClient.Builder restClientBuilder,
        @Value("${app.oauth.kakao.user-info-uri:https://kapi.kakao.com/v2/user/me}") String kakaoUserInfoUri,
        @Value("${app.oauth.naver.user-info-uri:https://openapi.naver.com/v1/nid/me}") String naverUserInfoUri,
        @Value("${app.oauth.google.user-info-uri:https://www.googleapis.com/oauth2/v3/userinfo}") String googleUserInfoUri
    ) {
        this.restClient = restClientBuilder.build();
        this.kakaoUserInfoUri = kakaoUserInfoUri;
        this.naverUserInfoUri = naverUserInfoUri;
        this.googleUserInfoUri = googleUserInfoUri;
    }

    @Override
    public OAuthUserInfo getUserInfo(OAuthProvider provider, String accessToken) {
        try {
            JsonNode response = restClient.get()
                    .uri(userInfoUri(provider))
                    .headers(headers -> headers.setBearerAuth(accessToken))
                    .retrieve()
                    .body(JsonNode.class);
            return parse(provider, response);
        } catch (RestClientException e) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED, e);
        }
    }

    private String userInfoUri(OAuthProvider provider) {
        return switch (provider) {
            case KAKAO -> kakaoUserInfoUri;
            case NAVER -> naverUserInfoUri;
            case GOOGLE -> googleUserInfoUri;
        };
    }

    private OAuthUserInfo parse(OAuthProvider provider, JsonNode response) {
        if (response == null || response.isNull()) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
        return switch (provider) {
            case KAKAO -> kakao(response);
            case NAVER -> naver(response);
            case GOOGLE -> google(response);
        };
    }

    private OAuthUserInfo kakao(JsonNode response) {
        String providerUserId = text(response, "id");
        JsonNode account = response.path("kakao_account");
        JsonNode profile = account.path("profile");
        return userInfo(
                OAuthProvider.KAKAO,
                providerUserId,
                text(account, "email"),
                text(profile, "nickname"),
                text(profile, "profile_image_url")
        );
    }

    private OAuthUserInfo naver(JsonNode response) {
        JsonNode profile = response.path("response");
        return userInfo(
                OAuthProvider.NAVER,
                text(profile, "id"),
                text(profile, "email"),
                text(profile, "nickname"),
                text(profile, "profile_image")
        );
    }

    private OAuthUserInfo google(JsonNode response) {
        return userInfo(
                OAuthProvider.GOOGLE,
                text(response, "sub"),
                text(response, "email"),
                text(response, "name"),
                text(response, "picture")
        );
    }

    private OAuthUserInfo userInfo(
        OAuthProvider provider,
        String providerUserId,
        String email,
        String nickname,
        String profileImageUrl
    ) {
        if (providerUserId == null || providerUserId.isBlank()) {
            throw new CustomException(ErrorCode.OAUTH_USER_INFO_FAILED);
        }
        return new OAuthUserInfo(provider, providerUserId, email, nickname, profileImageUrl);
    }

    private String text(JsonNode node, String fieldName) {
        JsonNode value = node.get(fieldName);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
