package com.developers.notify.controller;

import com.developers.notify.dto.PublishScheduleMentorRequest;
import com.developers.notify.entity.SubscriptionSchedule;
import com.developers.notify.service.SubscribeScheduleServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RabbitMQScheduleController {
    private final SubscribeScheduleServiceImpl subscribeScheduleService;

    @PostMapping("/publish/schedule")
    public ResponseEntity<String> publishMentor(@RequestBody PublishScheduleMentorRequest request) throws Exception{
        subscribeScheduleService.mentorPublishMessage(request);
        return ResponseEntity.status(HttpStatus.OK).body("메시지 전달 완료!");
    }

    @PostMapping("/subscribe/schedule")
    public ResponseEntity subscribeMentor(@RequestParam String mentorName,
                                          @RequestParam String userName,
                                          @RequestParam String email) throws Exception{
        subscribeScheduleService.subscribeMentor(mentorName, userName, email);
        return ResponseEntity.status(HttpStatus.OK).body("구독 완료!");

    }

    @GetMapping("/subscribe/schedule/listen")
    public ResponseEntity<SseEmitter> listenSchedulePush(@RequestParam String mentorName,
                                                         @RequestParam String userName,
                                                         @RequestParam String time,
                                                         @RequestParam String email) throws Exception{
        SseEmitter emitter = subscribeScheduleService.listenSchedulePush(mentorName, userName, time, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }

    @DeleteMapping("/unsubscribe/schedule")
    public ResponseEntity<String> unsubscribeMentor(@RequestParam String mentorName,
                                                    @RequestParam String userName) throws Exception{
        subscribeScheduleService.unsubscribeMentor(mentorName, userName);
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소 완료!");
    }

    @GetMapping("/subscriptions/schedule")
    public ResponseEntity<Object> getAllSubscriptions(@RequestParam String userName) throws Exception{
        List<SubscriptionSchedule> subscriptionSchedules = subscribeScheduleService.getAllSubscriptions(userName);
        return ResponseEntity.status(HttpStatus.OK)
                .body(subscriptionSchedules);
    }
}