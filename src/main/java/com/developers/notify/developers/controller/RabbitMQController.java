package com.developers.notify.developers.controller;

import com.developers.notify.developers.data.Subscribes;
import com.developers.notify.developers.service.MessageService;
import com.developers.notify.developers.service.SchedulerService;
import com.developers.notify.developers.service.UserSubscriptionService;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@AllArgsConstructor
@RequestMapping("/api")
public class RabbitMQController {
    private final RabbitTemplate rabbitTemplate;
    private final RabbitAdmin rabbitAdmin;
    // flux 메시지 반환
    private final MessageService messageService;
    // 구독 정보 반환
    private final UserSubscriptionService subscriptionService;
    // 스케쥴링 구현
    private final SchedulerService schedulerService;

    // 멘토가 메시지를 발행하는 이벤트를 동작했을 때
    @GetMapping("/publish")
    public ResponseEntity<String> publishMentor(){
        //추후 Push 부분은 매개변수 값으로 넘겨줄 에정

        // 문자열 익스체인지 생성
        String exchangeStr = "push"+"."+"exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);
        // 문자열 큐 생성
        String queStr = "push" + "." + "queue" + "." + "mentor1";
        Queue queue = new Queue(queStr, true, false, false);
        rabbitAdmin.declareQueue(queue);
        // 문자열 키 생성
        String routeStr = "push"+"."+"route"+"."+"mentor1";

        // Queue, Exchange 바인딩
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routeStr).noargs();
        rabbitAdmin.declareBinding(binding);

        // 메시지 생성
        String msg = "push"+"에 대한 메시지를 "+"mentor1"+"가 생성!";
        rabbitTemplate.convertAndSend(exchangeStr, routeStr, msg);
        return ResponseEntity.ok("발행 완료!");
    }

    // 멘토에 대한 구독
    //이 설정 이후부터 실시간 알림 발송 가능
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> subscribeMentor(){
        // 동적 구독
        //추후 prefix 는 매개변수 값으로 넘겨주어야 함
        String queStr = "push" + "." + "queue" + "." + "mentor1";
        return messageService.subscribeToMessages(queStr);
    }
    @GetMapping(value="/subscribe/schedule", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public void subscribeSchedule(){
        String queStr = "push" + "." + "queue" + "." + "mentor1";
        LocalDateTime notificationTime = LocalDateTime.of(2023,4,3,01,35);
        schedulerService.scheduleNotify(notificationTime, queStr);
    }

    // 멘토에 대한 구독
    //멘토 구독 정보를 DB에 저장
    //재접속 시 활용 필요
    @PostMapping("/subscribe")
    public ResponseEntity<Void> subscribe(@RequestParam String userId) {
        //추후 prefix 와 userid 는 동적 할당
        String id = UUID.randomUUID().toString();
        Subscribes subscription = new Subscribes(id, userId, "push.queue.mentor1");
        subscriptionService.save(subscription);
        return ResponseEntity.ok().build();
    }

    // 내가 구독한 정보 가져오기
    @GetMapping("/getSubscriptions")
    public Flux<String> getSubscriptions(@RequestParam String userId) {
        List<Subscribes> subscriptions = subscriptionService.findByUserId(userId);
        System.out.println(subscriptions.toString());
        List<Flux<String>> messageFluxes = subscriptions.stream()
                .map(subscription -> messageService.subscribeToMessages(subscription.getQueueName()))
                .collect(Collectors.toList());

        return Flux.merge(messageFluxes);
    }
}