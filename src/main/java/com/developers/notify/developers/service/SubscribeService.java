package com.developers.notify.developers.service;

import com.developers.notify.developers.entity.Subscription;
import com.developers.notify.developers.repository.UserSubscribeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubscribeService {
    private final UserSubscribeRepository userSubscribeRepository;

    public List<Subscription> getAllSubscriptions(Long userId) {
        return userSubscribeRepository.findByUser_id(userId);
    }

    public List<Long> getUserList(String mentorName) {
        List<Subscription> subscriptions = userSubscribeRepository.findByMentor_name(mentorName);
        return subscriptions.stream().map(Subscription::getUser_id).collect(Collectors.toList());
    }

    public void saveSubscription(Long userId, String mentorId) {
        // 중복 저장 방지
        Subscription existingSubscription = userSubscribeRepository.findByUserIdAndMentorId(userId, mentorId);
        if (existingSubscription == null) {
            Subscription subscription = new Subscription(userId, mentorId);
            userSubscribeRepository.save(subscription);
        }
    }

    public void deleteSubscription(Long userId, String mentorId) {
        // 삭제 조건 확인
        Subscription existingSubscription = userSubscribeRepository.findByUserIdAndMentorId(userId, mentorId);
        if (existingSubscription != null) {
            userSubscribeRepository.delete(existingSubscription);
        }
    }
}
