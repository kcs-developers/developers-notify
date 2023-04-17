package com.developers.notify.service;

import com.developers.notify.dto.PublishScheduleMentorRequest;
import com.developers.notify.entity.SubscriptionSchedule;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SubscribeScheduleService {

    public void mentorPublishMessage(PublishScheduleMentorRequest request) throws Exception;
    public void subscribeMentor(String mentorName, String userName, String email) throws Exception;
    public SseEmitter listenSchedulePush(String mentorName, String userName, String time, String email);
    public void unsubscribeMentor(String mentorName, String userName) throws Exception;
    ///
    public List<SubscriptionSchedule> getAllSubscriptions(String userName);
    public List<String> getUserList(String mentorName);
    public void saveSubscription(String userName, String mentorName);

    public void deleteSubscription(String userName, String mentorName);
}
