package com.developers.notify.developers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class MessageService {
    // 큐에서 메시지를 받을 때마다 Flux 형태로
    //fluxsink 를 통해 구독한 대상에게 .next 로 전달
    private final ConnectionFactory connectionFactory;
    public void subscribeToMessages(String queueName, SseEmitter emitter) {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueueNames(queueName);
            // 수신 메시지 처리
            container.setMessageListener((MessageListener) message -> {
                String payload = new String(message.getBody(), StandardCharsets.UTF_8);
                try {
                    emitter.send(SseEmitter.event().name("message").data(payload));
                } catch (IOException e) {
                    if (e.getCause() instanceof ClientAbortException) {
                        // 클라이언트 연결이 끊어진 경우, 에러 메시지를 출력하고 리스너를 종료
                        System.out.println("Client disconnected");
                        container.stop();
                    } else {
                        e.printStackTrace();
                    }
                }
            });
            container.start();
            emitter.onCompletion(() -> container.isAutoStartup());
    }
}
