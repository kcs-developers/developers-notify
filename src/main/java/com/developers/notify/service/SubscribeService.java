package com.developers.notify.service;

import com.developers.notify.dto.push.PublishMentorRequest;
import com.developers.notify.entity.Subscription;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
;

public interface SubscribeService {
    public void mentorPublishMessage(PublishMentorRequest request) throws Exception;
    public List<Subscription> subscribeMentor(String mentorName, String userName, String email, String roomName, String startTime) throws Exception;
    public SseEmitter listenPush(String mentorName, String userName, String email);
    public List<Subscription> unsubscribeMentor(String mentorName, String userName, String roomName) throws Exception;
    ///
    public List<Subscription> getAllSubscriptions(String userName);
    public List<String> getUserList(String mentorName);
    public void saveSubscription(String userName, String mentorName, String roomName, String startTime);

    public void deleteSubscription(String userName, String mentorName, String roomName);
}
