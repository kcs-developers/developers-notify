package com.developers.notify.developers.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@AllArgsConstructor
public class MessageService {
    // 큐에서 메시지를 받을 때마다 Flux 형태로
    //fluxsink 를 통해 구독한 대상에게 .next 로 전달
    private final ConnectionFactory connectionFactory;
    public Flux<String> subscribeToMessages(String queueName) {
        return Flux.<String>create(sink -> {
            SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
            container.setQueueNames(queueName);
            // 수신 메시지 처리
            container.setMessageListener((MessageListener) message -> {
                String payload = new String(message.getBody(), StandardCharsets.UTF_8);
                String event = "data: " + payload + "\n\n";
                System.out.println(event);
                sink.next(event);
            });
            container.start();
            sink.onCancel(() -> container.stop());
        }).share(); // 모든 구독자에게 메시지를 공유하기 위해 share()를 추가합니다.
    }
}
