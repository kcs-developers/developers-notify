package com.developers.notify.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@AllArgsConstructor
@Log4j2
public class MessageService {
    private final ConnectionFactory connectionFactory;
    private final MailSendService mailSendService;
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public void subscribeToMessages(String queueName, String userName, SseEmitter emitter, String email) throws MailSendException, ParseException {
        SimpleMessageListenerContainer container = createMessageListenerContainer(queueName);
        userEmitters.put(userName, emitter);

        container.setMessageListener((MessageListener) message -> {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            try {
                sendMailIfEmailIsNotEmpty(email, payload);
                log.info("전송될 메시지: " + payload);
                emitter.send(SseEmitter.event().name("push").data(payload));
            } catch (IOException | ParseException e) {
                handleIOException(container, e);
            }
        });

        emitter.onCompletion(() -> {
            log.info(userName + " 의 푸시 알림 객체 삭제! ");
            container.stop();
            userEmitters.remove(userName);
            log.info(userName + " 의 메시지 전달 객체 삭제");
        });
    }

    private SimpleMessageListenerContainer createMessageListenerContainer(String queueName) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        return container;
    }

    private void sendMailIfEmailIsNotEmpty(String email, String payload) throws MailSendException, ParseException {
        if (email != null) {
            try {
                mailSendService.sendMail(email, payload);
            }catch(Exception e){
                log.error("메일 발송 오류!");
            }
            log.info(email + "로 " + payload + "전송!");
        }
    }

    private void handleIOException(SimpleMessageListenerContainer container, Exception e) {
        if (e.getCause() instanceof ClientAbortException) {
            log.error("비정상적 클라이언트 종료!");
            container.stop();
        } else {
            log.error(e.getMessage());
            throw new EmitterException("sse 이벤트 메시지 생성 오류!");
        }
    }
}
