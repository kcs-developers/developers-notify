package com.developers.notify.developers.controller;

import com.developers.notify.developers.entity.SubscriptionSchedule;
import com.developers.notify.developers.service.SubscribeScheduleServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RabbitMQScheduleController {
    private final SubscribeScheduleServiceImpl subscribeScheduleService;

    @GetMapping("/publish/schedule")
    public ResponseEntity<String> publishMentor(@RequestParam String mentorName) {
        subscribeScheduleService.mentorPublishMessage(mentorName);
        return ResponseEntity.ok("발행 완료!");
    }

    @GetMapping(value="/subscribe/schedule", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<String> subscribeSchedule(@RequestParam String mentorName,
                                        @RequestParam String userName,
                                        @RequestParam String time){
        subscribeScheduleService.subscribeMentorAndMessage(mentorName, userName, time);
        return ResponseEntity.ok("메시지 예약 완료!");
    }

    @DeleteMapping("/unsubscribe/schedule")
    public ResponseEntity<String> unsubscribeMentor(@RequestParam String mentorName,
                                                    @RequestParam String userName){
        subscribeScheduleService.unsubscribeMentor(mentorName, userName);
        return ResponseEntity.ok("구독 취소 완료!");
    }

    @GetMapping("/subscriptions/schedule")
    public ResponseEntity<Object> getAllSubscriptions(@RequestParam String userName){
        List<SubscriptionSchedule> subscriptionSchedules = subscribeScheduleService.getAllSubscriptions(userName);
        return ResponseEntity.status(HttpStatus.OK)
                .body(subscriptionSchedules);
    }
}