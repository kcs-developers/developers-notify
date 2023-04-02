package com.developers.notify.developers.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class SchedulerConfig {
    @Bean
    public ScheduledExecutorService scheduledExecutorService() {
        // 스레드 풀 크기 조절?
        return Executors.newScheduledThreadPool(8);
    }
}
