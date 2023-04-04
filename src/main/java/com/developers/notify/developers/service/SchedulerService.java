package com.developers.notify.developers.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
@Log4j2
public class SchedulerService {
    private final ScheduledExecutorService scheduledExecutorService;
    private final MessageService messageService;


    public void scheduleNotify(LocalDateTime notificationTime, String queName, String userName, SseEmitter emitter){
        long delay = Duration.between(LocalDateTime.now(), notificationTime.minusMinutes(10)).toMillis();
        log.info("---메시지 발송 예약---");
        scheduledExecutorService.schedule(() -> {
            messageService.subscribeToMessages(queName, userName, emitter);
        }, delay, TimeUnit.MILLISECONDS);
    }
}
