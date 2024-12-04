package com.sns.feedserver.feed;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.ZonedDateTime;

@Entity
@Table
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SocialFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int feedId;
    private String imageId;
    private int uploaderId;
    @CreatedDate
    private ZonedDateTime uploadDatetime;
    private String contents;

    public SocialFeed(String imageId, int uploaderId, String contents) {
        this.imageId = imageId;
        this.uploaderId = uploaderId;
        this.contents = contents;
    }

    public SocialFeed(FeedRequest request) {
        this(request.getImageId(), request.getUploaderId(), request.getContents());
    }
}