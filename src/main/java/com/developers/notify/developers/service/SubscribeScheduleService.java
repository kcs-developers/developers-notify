package com.developers.notify.developers.service;

import com.developers.notify.developers.entity.SubscriptionSchedule;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface SubscribeScheduleService {

    public void mentorPublishMessage(String mentorName);
    public SseEmitter subscribeMentorAndMessage(String mentorName, String userName, String time);
    public void unsubscribeMentor(String mentorName, String userName);
    ///
    public List<SubscriptionSchedule> getAllSubscriptions(String userName);
    public List<String> getUserList(String mentorName);
    public void saveSubscription(String userName, String mentorName);

    public void deleteSubscription(String userName, String mentorName);
}
