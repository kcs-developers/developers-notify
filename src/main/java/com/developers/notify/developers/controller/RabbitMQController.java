package com.developers.notify.developers.controller;

import com.developers.notify.developers.service.MessageService;
import com.developers.notify.developers.service.SchedulerService;
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
    private final SseEmitter emitter = new SseEmitter(-1L);


    // 멘토가 메시지를 발행하는 이벤트를 동작했을 때
    @GetMapping("/publish")
    public ResponseEntity<String> publishMentor(@RequestParam String mentorId){
        //추후 Push 부분은 매개변수 값으로 넘겨줄 에정

        // 문자열 익스체인지 생성
        String exchangeStr = "push"+"."+"exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);
        // 문자열 큐 생성
        String queStr = "push" + "." + "queue" + "." + mentorId;
        Queue queue = new Queue(queStr, true, false, false);
        rabbitAdmin.declareQueue(queue);
        // 문자열 키 생성
        String routeStr = "push"+"."+"route"+"."+mentorId;

        // Queue, Exchange 바인딩
        Binding binding = BindingBuilder.bind(queue).to(exchange).with(routeStr).noargs();
        rabbitAdmin.declareBinding(binding);

        // 메시지 생성
        String msg = "push"+"에 대한 메시지를 "+mentorId+"가 생성!";
        rabbitTemplate.convertAndSend(exchangeStr, routeStr, msg);
        return ResponseEntity.ok("발행 완료!");
    }

    // 멘토에 대한 구독
    //이 설정 이후부터 실시간 알림 발송 가능
    @GetMapping(value = "/subscribe")
    public SseEmitter subscribeMentor(@RequestParam String mentorId){
        // 동적 구독
        //추후 prefix 는 매개변수 값으로 넘겨주어야 함
        String queStr = "push" + "." + "queue" + "." + mentorId;
        SseEmitter emitter = new SseEmitter();
        messageService.subscribeToMessages(queStr, emitter);
        return emitter;
    }
    @GetMapping(value="/subscribe/schedule", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeSchedule(@RequestParam String mentorId, @RequestParam String time){
        String queStr = "push" + "." + "queue" + "." + mentorId;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime notificationTime = LocalDateTime.parse(time, formatter);
        SseEmitter emitter = new SseEmitter(-1L);
        schedulerService.scheduleNotify(notificationTime, queStr, emitter);
        return emitter;

    }

    // 멘토에 대한 구독
    //멘토 구독 정보를 DB에 저장
    //재접속 시 활용 필요
}