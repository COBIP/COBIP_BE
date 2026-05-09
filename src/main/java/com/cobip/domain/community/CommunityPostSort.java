package com.cobip.domain.community;

import org.springframework.data.domain.Sort;

public enum CommunityPostSort {
    LATEST,
    POPULAR;

    public Sort toSort() {
        if (this == POPULAR) {
            return Sort.by(
                    Sort.Order.desc("likeCount"),
                    Sort.Order.desc("commentCount"),
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createdAt")
            );
        }
        return Sort.by(Sort.Order.desc("createdAt"));
    }
}
