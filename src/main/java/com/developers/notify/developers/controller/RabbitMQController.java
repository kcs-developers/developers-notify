package com.developers.notify.developers.controller;

import com.developers.notify.developers.entity.Subscription;
import com.developers.notify.developers.service.SubscribeServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RabbitMQController {
    private final SubscribeServiceImpl subscribeServiceImpl; // 구독 정보 DB 저장


    // 멘토의 메시지 발행
    @GetMapping("/publish")
    public ResponseEntity<String> publishMentor(@RequestParam String mentorName,
                                                @RequestBody String message) {
        subscribeServiceImpl.mentorPublishMessage(mentorName, message);
        return ResponseEntity.ok("발행 완료!");
    }


    // 멘토에 대한 구독
    //이 설정 이후부터 실시간 알림 발송 가능
    @GetMapping(value = "/subscribe")
    public ResponseEntity<String> subscribeMentor(@RequestParam String mentorName,
                                      @RequestParam String userName){
        subscribeServiceImpl.subscribeMentorAndMessage(mentorName, userName);
        return ResponseEntity.ok("구독 및 메시지 수신 가능!");
    }

    // 멘토 구독에 대한 삭제 이벤트
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribeMentor(@RequestParam String mentorName,
                                                    @RequestParam String userName){
        subscribeServiceImpl.unsubscribeMentor(mentorName, userName);
        return ResponseEntity.ok("구독 취소 완료!");
    }

    // 멘토 구독 정보 가져오기
    //클라이언트에서 다시 fetch 요청 보내야함
    @GetMapping("/subscriptions")
    public ResponseEntity<Object> getAllSubscriptions(@RequestParam String userName){
        List<Subscription> subscriptions = subscribeServiceImpl.getAllSubscriptions(userName);
        return ResponseEntity.status(HttpStatus.OK)
                .body(subscriptions);
    }
}