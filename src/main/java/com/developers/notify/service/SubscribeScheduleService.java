package com.developers.notify.service;

import com.developers.notify.dto.schedule.PublishScheduleMentorRequest;
import com.developers.notify.entity.ScheduleSubscription;
import com.developers.notify.entity.Subscription;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.util.List;

public interface SubscribeScheduleService {

    public void mentorPublishMessage(PublishScheduleMentorRequest request) throws Exception;
    public List<ScheduleSubscription> subscribeMentor(String mentorName, String userName, String email, String roomName, LocalDateTime startTime) throws Exception;
    public SseEmitter listenSchedulePush(String mentorName, String userName, String time, String email);
    public List<ScheduleSubscription> unsubscribeMentor(String mentorName, String userName, String roomName) throws Exception;
    ///
    public List<ScheduleSubscription> getAllSubscriptions(String userName);
    public List<String> getUserList(String mentorName);
    public void saveSubscription(String userName, String mentorName, String roomName, LocalDateTime startTime);

    public void deleteSubscription(String userName, String mentorName, String roomName);
}
