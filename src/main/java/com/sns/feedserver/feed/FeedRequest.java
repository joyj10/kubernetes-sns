package com.sns.feedserver.feed;

import lombok.Getter;

@Getter
public class FeedRequest {
    private String imageId;
    private int uploaderId;
    private String contents;
}
