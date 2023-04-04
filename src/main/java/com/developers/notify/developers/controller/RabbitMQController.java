package com.developers.notify.developers.controller;

import com.developers.notify.developers.entity.Subscription;
import com.developers.notify.developers.repository.UserSubscribeRepository;
import com.developers.notify.developers.service.MessageService;
import com.developers.notify.developers.service.SchedulerService;
import com.developers.notify.developers.service.SubscribeService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RabbitMQController {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    // flux 메시지 반환
    private final MessageService messageService;
    // 스케쥴링 구현
    private final SchedulerService schedulerService;
    // 저장
    private final SubscribeService subscribeService;
    private final UserSubscribeRepository userSubscribeRepository;
    private final SseEmitter emitter = new SseEmitter(-1L);

    // 멘토가 메시지를 발행하는 이벤트를 동작했을 때
    @GetMapping("/publish")
    public ResponseEntity<String> publishMentor(@RequestParam String mentorId) {
        // 문자열 익스체인지 생성
        String exchangeStr = "push" + "." + "exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);

        // 해당 멘토를 구독하는 모든 사용자의 아이디 목록
        List<Long> userIds = subscribeService.getUserList(mentorId);

        // 각 사용자의 큐에 메시지를 전송
        for (Long userId : userIds) {
            // 문자열 큐 생성
            String queStr = "push" + "." + "queue" + "." + mentorId + "." + userId;
            Queue queue = new Queue(queStr, true, false, false);
            rabbitAdmin.declareQueue(queue);
            // 문자열 키 생성
            String routeStr = "push" + "." + "route" + "." + mentorId + "." + userId;

            // Queue, Exchange 바인딩
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(routeStr).noargs();
            rabbitAdmin.declareBinding(binding);

            // 메시지 생성
            String msg = "push" + "에 대한 메시지를 " + mentorId + "가 생성!";
            rabbitTemplate.convertAndSend(exchangeStr, routeStr, msg);
        }
        return ResponseEntity.ok("발행 완료!");
    }


    // 멘토에 대한 구독
    //이 설정 이후부터 실시간 알림 발송 가능
    @GetMapping(value = "/subscribe")
    public SseEmitter subscribeMentor(@RequestParam String mentorId,
                                      @RequestParam Long userId){
        // 동적 구독
        //추후 prefix 는 매개변수 값으로 넘겨주어야 함
        String queStr = "push" + "." + "queue" + "." + mentorId;
        SseEmitter emitter = new SseEmitter();
        messageService.subscribeToMessages(queStr, userId, emitter);

        // 사용자 정보를 가져와서 저장
        subscribeService.saveSubscription(userId, mentorId);
        return emitter;
    }

    // 멘토 구독에 대한 삭제 이벤트
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribeMentor(@RequestParam String mentorId,
                                                    @RequestParam Long userId){
        String queStr = "push" + "." + "queue" + "." + mentorId;
        subscribeService.deleteSubscription(userId, mentorId);
        return ResponseEntity.ok("구독 취소 완료!");
    }

    // 멘토 구독 정보 가져오기
    //클라이언트에서 다시 fetch 요청 보내야함
    @GetMapping("/subscriptions")
    public List<Subscription> getAllSubscriptions(@RequestParam Long userId) {
        return subscribeService.getAllSubscriptions(userId);
    }


    @GetMapping(value="/subscribe/schedule", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeSchedule(@RequestParam String mentorId,
                                        @RequestParam Long userId,
                                        @RequestParam String time){
        String queStr = "push" + "." + "queue" + "." + mentorId;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime notificationTime = LocalDateTime.parse(time, formatter);
        SseEmitter emitter = new SseEmitter(-1L);
        schedulerService.scheduleNotify(notificationTime, queStr, userId, emitter);

        // 사용자 정보를 가져와서 저장
        Subscription member = new Subscription(userId, mentorId);
//        userSubscribeRepository.save(member);
//        userSubscribeScheduleRepository.save();

        return emitter;

    }

    // 멘토에 대한 구독
    //멘토 구독 정보를 DB에 저장
    //재접속 시 활용 필요
}