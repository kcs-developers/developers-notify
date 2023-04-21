package com.developers.notify.controller;

import com.developers.notify.dto.push.DeleteMentorRequest;
import com.developers.notify.dto.push.PublishMentorRequest;
import com.developers.notify.dto.push.SubscribeMentorRequest;
import com.developers.notify.entity.Subscription;
import com.developers.notify.service.SubscribeServiceImpl;
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
public class RabbitMQController {
    private final SubscribeServiceImpl subscribeServiceImpl; // 구독 정보 DB 저장


    // 멘토의 메시지 발행
    @PostMapping("/publish")
    public ResponseEntity<String> publishMentor(@RequestBody PublishMentorRequest request) throws Exception {
        subscribeServiceImpl.mentorPublishMessage(request);
        return ResponseEntity.ok("발행 완료!");
    }


    // 멘토에 대한 구독
    //이 설정 이후부터 실시간 알림 발송 가능
    @PostMapping(value = "/subscribe")
    public ResponseEntity subscribeMentor(@RequestBody SubscribeMentorRequest request) throws Exception{
        subscribeServiceImpl.subscribeMentor(request.getMentorName(), request.getUserName(), request.getEmail());
        return ResponseEntity.status(HttpStatus.OK).body("구독 완료!");
    }

    @GetMapping("/listen")
    public ResponseEntity<SseEmitter> listenPush(@RequestParam String mentorName,
                                                 @RequestParam String userName,
                                                 @RequestParam String email) throws Exception{
        String mentorNameDecoded = URLDecoder.decode(mentorName, StandardCharsets.UTF_8);
        String userNameDecoded = URLDecoder.decode(userName, StandardCharsets.UTF_8);
        log.info("listen params..."+"Mentor: "+mentorNameDecoded+"User: "+userNameDecoded);

        SseEmitter emitter = subscribeServiceImpl.listenPush(mentorName, userName, email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_EVENT_STREAM);
        return new ResponseEntity<>(emitter, headers, HttpStatus.OK);
    }

    // 멘토 구독에 대한 삭제 이벤트
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribeMentor(@RequestBody DeleteMentorRequest request) throws Exception{
        subscribeServiceImpl.unsubscribeMentor(request.getMentorName(), request.getUserName());
        return ResponseEntity.status(HttpStatus.OK).body("구독 취소 완료!");
    }

    // 멘토 구독 정보 가져오기
    //클라이언트에서 다시 fetch 요청 보내야함
    @GetMapping("/subscriptions")
    public ResponseEntity<Object> getAllSubscriptions(@RequestParam String userName) throws Exception{
        String userNameDecoded = URLDecoder.decode(userName, StandardCharsets.UTF_8);
        log.info("subscriptions params...",userNameDecoded);

        List<Subscription> subscriptions = subscribeServiceImpl.getAllSubscriptions(userName);
        return ResponseEntity.status(HttpStatus.OK)
                .body(subscriptions);
    }
}