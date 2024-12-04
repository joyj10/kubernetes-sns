package com.sns.feedserver.feed;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialFeedService {
    private final SocialFeedRepository feedRepository;

    @Value("${sns.user-server}")
    private String userServerUrl;
    private final RestClient restClient = RestClient.create();

    public List<SocialFeed> getAllFeeds() {
        return feedRepository.findAll();
    }

    public List<SocialFeed> getAllFeedsByUploaderId(int uploaderId) {
        return feedRepository.findByUploaderId(uploaderId);
    }

    public SocialFeed getFeedById(int feedId) {
        return feedRepository.findById(feedId)
                .orElseThrow(EntityNotFoundException::new);
    }

    @Transactional
    public void deleteFeed(int feedId) {
        feedRepository.deleteById(feedId);
    }

    @Transactional
    public SocialFeed createFeed(FeedRequest feed) {
        return feedRepository.save(new SocialFeed(feed));
    }

    public UserInfo getUserInfo(int userId) {
        return restClient.get()
                .uri("%s/api/users/%s".formatted(userServerUrl, userId))
                .retrieve()
                .onStatus(HttpStatusCode::isError, ((request, response) -> {
                    throw new RuntimeException("invalid server response : %s".formatted(response.getStatusText()));
                }))
                .body(UserInfo.class);

    }
}
