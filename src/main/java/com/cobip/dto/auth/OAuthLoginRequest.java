package com.cobip.dto.auth;

import com.cobip.domain.oauth.OAuthProvider;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OAuthLoginRequest {

    @NotNull
    private OAuthProvider provider;

    @NotBlank
    @Size(max = 4000)
    private String accessToken;

    @Size(max = 60)
    private String nickname;
}
