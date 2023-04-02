package com.developers.notify.developers.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@AllArgsConstructor
public class SchedulerService {
    private final ScheduledExecutorService scheduledExecutorService;
    private final MessageService messageService;

    public void scheduleNotify(LocalDateTime notificationTime, String queName){
        long delay = Duration.between(LocalDateTime.now(), notificationTime.minusMinutes(10)).toMillis();

        scheduledExecutorService.schedule(()->messageService.subscribeToMessages(queName), delay, TimeUnit.MILLISECONDS);
    }
}
