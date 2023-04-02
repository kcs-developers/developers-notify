package com.developers.notify.developers.service;

import com.developers.notify.developers.data.Subscribes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class UserSubscriptionService {
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private static final String SUBSCRIPTIONS_KEY = "userSubscribe";


    // 사용자에 대해 구독 정보 저장
    public void save(Subscribes subscribes) {
        String subscriptionJson;
        try {
            subscriptionJson = objectMapper.writeValueAsString(subscribes);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("유저 정보 json 변경 오류", e);
        }
        stringRedisTemplate.opsForHash().put(SUBSCRIPTIONS_KEY, subscribes.getId(), subscriptionJson);
    }

    // 사용자에 대해 전체 구독 정보 가져오기
    public List<Subscribes> findByUserId(String userId) {
        Map<Object, Object> subscriptionMap = stringRedisTemplate.opsForHash().entries(SUBSCRIPTIONS_KEY);
        return subscriptionMap.values().stream()
                .map(json -> {
                    try {
                        return objectMapper.readValue((String) json, Subscribes.class);
                    } catch (IOException e) {
                        throw new RuntimeException("json 유저 변환 오류", e);
                    }
                })
                .filter(subscription -> subscription.getUserId().equals(userId))
                .collect(Collectors.toList());
    }
}
