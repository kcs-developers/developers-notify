package com.developers.notify.developers.service;

import com.developers.notify.developers.dto.PublishMentorRequest;
import com.developers.notify.developers.entity.Subscription;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
;

public interface SubscribeService {
    public void mentorPublishMessage(PublishMentorRequest request) throws Exception;
    public void subscribeMentor(String mentorName, String userName, String email) throws Exception;
    public SseEmitter listenPush(String mentorName, String userName, String email);
    public void unsubscribeMentor(String mentorName, String userName) throws Exception;
    ///
    public List<Subscription> getAllSubscriptions(String userName);
    public List<String> getUserList(String mentorName);
    public void saveSubscription(String userName, String mentorName);

    public void deleteSubscription(String userName, String mentorName);
}
