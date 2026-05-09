package com.cobip.dto.community;

import com.cobip.domain.community.CommunityPost;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Getter;

@Getter
public class CommunityPostDetailResponse extends CommunityPostSummaryResponse {

    private final JsonNode contentJson;

    private CommunityPostDetailResponse(CommunityPost post) {
        super(post);
        this.contentJson = post.getContentJson();
    }

    public static CommunityPostDetailResponse from(CommunityPost post) {
        return new CommunityPostDetailResponse(post);
    }
}
