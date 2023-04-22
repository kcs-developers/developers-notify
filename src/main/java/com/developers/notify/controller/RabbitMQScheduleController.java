package com.developers.notify.controller;

import com.developers.notify.dto.schedule.DeleteScheduleMentorRequest;
import com.developers.notify.dto.schedule.PublishScheduleMentorRequest;
import com.developers.notify.dto.schedule.SubscribeScheduleMentorRequest;
import com.developers.notify.entity.ScheduleSubscription;
import com.developers.notify.service.SubscribeScheduleServiceImpl;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
@Log4j2
public class RabbitMQScheduleController {
    private final SubscribeScheduleServiceImpl subscribeScheduleService;

    @PostMapping("/publish/schedule")
    public ResponseEntity<String> publishMentor(@RequestBody PublishScheduleMentorRequest request) throws Exception{
        subscribeScheduleService.mentorPublishMessage(request);
        return ResponseEntity.status(HttpStatus.OK).body("메시지 전달 완료!");
    }

    @PostMapping("/subscribe/schedule")
    public ResponseEntity<?> subscribeMentor(@RequestBody SubscribeScheduleMentorRequest request) throws Exception{
        List<ScheduleSubscription> newSubscriptions = subscribeScheduleService.subscribeMentor(request.getMentorName(), request.getUserName(), request.getEmail(), request.getRoomName(), request.getStartTime());
        return ResponseEntity.status(HttpStatus.OK).body(newSubscriptions);

    }

    @GetMapping("/listen/schedule")
    public ResponseEntity<SseEmitter> listenSchedulePush(@RequestParam String mentorName,
                                                         @RequestParam String userName,
                                                         @RequestParam String time,
                                                         @RequestParam String email) throws Exception{
        String mentorNameDecoded = URLDecoder.decode(mentorName, StandardCharsets.UTF_8);
        String userNameDecoded = URLDecoder.decode(userName, StandardCharsets.UTF_8);
        log.info("listen params...",mentorNameDecoded, userNameDecoded);

        SseEmitter emitter = subscribeScheduleService.listenSchedulePush(mentorNameDecoded, userNameDecoded, time, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }

    @DeleteMapping("/unsubscribe/schedule")
    public ResponseEntity<?> unsubscribeMentor(@RequestBody DeleteScheduleMentorRequest request) throws Exception{
        List<ScheduleSubscription> newSubscriptions = subscribeScheduleService.unsubscribeMentor(request.getMentorName(), request.getUserName(), request.getRoomName());
        return ResponseEntity.status(HttpStatus.OK).body(newSubscriptions);
    }

    @GetMapping("/subscriptions/schedule")
    public ResponseEntity<Object> getAllSubscriptions(@RequestParam String userName) throws Exception{
        String userNameDecoded = URLDecoder.decode(userName, StandardCharsets.UTF_8);
        log.info("subscriptions params...",userNameDecoded);

        List<ScheduleSubscription> subscriptionSchedules = subscribeScheduleService.getAllSubscriptions(userName);
        return ResponseEntity.status(HttpStatus.OK)
                .body(subscriptionSchedules);
    }
}