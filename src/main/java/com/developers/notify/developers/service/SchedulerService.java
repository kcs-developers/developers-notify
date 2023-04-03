package com.developers.notify.developers.service;

import lombok.AllArgsConstructor;
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
public class SchedulerService {
    private final ScheduledExecutorService scheduledExecutorService;
    private final MessageService messageService;


    public void scheduleNotify(LocalDateTime notificationTime, String queName, SseEmitter emitter){
        long delay = Duration.between(LocalDateTime.now(), notificationTime.minusMinutes(5)).toMillis();
        System.out.println(delay+"에 전달 예정!");
        scheduledExecutorService.schedule(() -> {
            messageService.subscribeToMessages(queName, emitter);
        }, delay, TimeUnit.MILLISECONDS);
    }
}
