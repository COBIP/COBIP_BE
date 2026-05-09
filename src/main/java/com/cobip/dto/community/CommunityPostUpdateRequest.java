package com.cobip.dto.community;

import com.cobip.domain.community.CommunityPostCategory;
import com.fasterxml.jackson.databind.JsonNode;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CommunityPostUpdateRequest {

    private CommunityPostCategory category;

    @Size(max = 120, message = "게시글 제목은 120자 이하로 입력해야 합니다.")
    private String title;

    private JsonNode contentJson;
}
