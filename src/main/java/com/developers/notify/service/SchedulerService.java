package com.developers.notify.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.ExchangeBuilder;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Log4j2
public class SchedulerService {
    private final ScheduledExecutorService scheduledExecutorService;
    private final MessageScheduleService messageScheduleService;
    private final RabbitAdmin rabbitAdmin;
    private final RabbitTemplate rabbitTemplate;


    public void scheduleNotify(LocalDateTime notificationTime, String queName, String userName, SseEmitter emitter, String email, String  mentorName, String roomName){
        long delay = Duration.between(LocalDateTime.now(), notificationTime.minusMinutes(10)).toMillis();
        log.info(mentorName+" 의 "+roomName+" 에 대한 알림 발송 예약! "+TimeUnit.MILLISECONDS.toMinutes(delay)+" 후 발송!");
        scheduledExecutorService.schedule(() -> {
            // 모든 동적 문자열에는 push.schedule 로 추가
            String exchangeStr = "push.schedule.exchange";
            Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
            rabbitAdmin.declareExchange(exchange);

            // 사용자 큐에 메시지 발송
                    String routeStr = "push.schedule.route." + mentorName + "." + userName+"."+roomName;
                    String message = "schedule-"+mentorName+" 멘토의 "+roomName+" 이 시작되기 10분 전입니다!\nhttps://diveloper.site/mentoring 에서 확인하세요!";
                    rabbitTemplate.convertAndSend(exchangeStr, routeStr, message);
            try {
                messageScheduleService.subscribeToMessages(queName, userName, emitter, email);
            } catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }, delay, TimeUnit.MILLISECONDS);
    }
}
