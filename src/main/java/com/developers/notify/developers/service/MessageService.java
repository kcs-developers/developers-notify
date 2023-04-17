package com.developers.notify.developers.service;

import com.developers.notify.developers.repository.UserSubscribeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.catalina.connector.ClientAbortException;
import org.springframework.amqp.core.MessageListener;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.mail.MailSendException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;
import reactor.core.publisher.Flux;

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

    public void subscribeToMessages(String queueName, String userName, SseEmitter emitter, String email) throws MailSendException, ParseException{
        // 큐에 대한 구독
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory);
        container.setQueueNames(queueName);
        userEmitters.put(userName, emitter);
        // 수신 메시지 처리
        container.setMessageListener((MessageListener) message -> {
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            try {
                log.info(email+"로 "+payload+"전송!");
                mailSendService.sendMail(email, payload);
            } catch (MailSendException | ParseException e) {
                log.error("메일 발송 서비스 오류! ", e.getCause());
                throw new MailSendException(e.getMessage());
            }
            try {
                log.info("전송될 메시지: "+payload);
                emitter.send(SseEmitter.event().name("push").data(payload));
            } catch (IOException e) {
                if (e.getCause() instanceof ClientAbortException) {
                    // 클라이언트 연결이 끊어진 경우, 에러 메시지를 출력하고 리스너를 종료
                    log.error("비정상적 클라이언트 종료!");
                    container.stop();
                } else {
                    log.error(e.getMessage());
                    throw new EmitterException("sse 이벤트 메시지 생성 오류!");
                }
            }
        });
        container.start();
        emitter.onCompletion(() -> {
            log.error(userName + " 의 푸시 알림 객체 삭제! ");
            container.stop();
            userEmitters.remove(userName);
            log.info(userName+" 의 메시지 전달 객체 삭제");
        });
    }
}
