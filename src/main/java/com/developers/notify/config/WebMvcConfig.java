package com.developers.notify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Bean
    public ThreadPoolTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(Runtime.getRuntime().availableProcessors()); // 적절한 스레드 풀 크기로 설정하세요.
        taskExecutor.setMaxPoolSize(50); // 적절한 최대 스레드 풀 크기로 설정하세요.
        taskExecutor.setQueueCapacity(100); // 적절한 대기 큐 크기로 설정하세요.
        taskExecutor.setThreadNamePrefix("mvc-task-executor-");
        taskExecutor.initialize();
        return taskExecutor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setTaskExecutor(mvcTaskExecutor());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 경로에 대해 적용
                .allowedOrigins("*") // 모든 도메인 허용
                .allowedMethods("*") // 모든 HTTP 메서드 허용
                .allowedHeaders("*")
                .maxAge(3600); // 캐시 유효 시간 설정
    }
}
