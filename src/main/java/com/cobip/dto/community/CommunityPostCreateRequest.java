package com.cobip.dto.community;

import com.cobip.domain.community.CommunityPostCategory;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommunityPostCreateRequest {

    @NotNull(message = "게시글 카테고리는 필수입니다.")
    private CommunityPostCategory category;

    @NotBlank(message = "게시글 제목은 필수입니다.")
    @Size(max = 120, message = "게시글 제목은 120자 이하로 입력해야 합니다.")
    private String title;

    @NotNull(message = "게시글 본문은 필수입니다.")
    private JsonNode contentJson;
}
