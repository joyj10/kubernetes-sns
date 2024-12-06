package com.sns.feedserver.feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SocialFeedService {
    private final SocialFeedRepository feedRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        SocialFeed savedFeed = feedRepository.save(new SocialFeed(feed));
        sendFeedToKafka(savedFeed);
        return savedFeed;
    }

    public void refreshAllFeeds() {
        List<SocialFeed> feeds = getAllFeeds();
        for (SocialFeed feed : feeds) {
            sendFeedToKafka(feed);
        }
    }

    private void sendFeedToKafka(SocialFeed feed) {
        UserInfo uploader = getUserInfo(feed.getUploaderId());
        FeedInfo feedInfo = new FeedInfo(feed, uploader.getUsername());
        try {
            kafkaTemplate.send("feed.created", objectMapper.writeValueAsString(feedInfo));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
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
