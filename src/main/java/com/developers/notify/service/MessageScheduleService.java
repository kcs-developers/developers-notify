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
public class MessageScheduleService {
    private final ConnectionFactory connectionFactory;
    private final MailSendService mailSendService;
    private final Map<String, SseEmitter> userEmitters = new ConcurrentHashMap<>();

    public void subscribeToMessages(String queueName, String userName, SseEmitter emitter, String email) throws MailSendException, ParseException {
        SimpleMessageListenerContainer scheduleContainer = createMessageListenerContainer(queueName);
        userEmitters.put(userName, emitter);

        scheduleContainer.setMessageListener((MessageListener) message -> {
            log.info("스케쥴 메시지 리스너: "+queueName);
            String payload = new String(message.getBody(), StandardCharsets.UTF_8);
            try {
                sendMailIfEmailIsNotEmpty(email, payload);
                log.info("전송될 스케쥴 메시지: " + payload);
            } catch (ParseException e) {
                handleIOException(scheduleContainer, e);
            }
            try{
                emitter.send(SseEmitter.event().name("schedule").data(payload));
                log.info("푸시될 스케쥴 메시지: "+payload);
            }catch (IOException e){
                log.error(e);
                handleIOException(scheduleContainer, e);
            }
        });

        try {
            scheduleContainer.start();
        } catch (Exception e) {
            log.error("스케쥴 컨테이너 시작 중 에러 발생: ", e);
        }

        emitter.onCompletion(() -> {
            log.info(userName + " 의 스케쥴 푸시 알림 객체 삭제! ");
            scheduleContainer.stop();
            userEmitters.remove(userName);
            log.info(userName + " 의 스케쥴 메시지 전달 객체 삭제");
        });

        emitter.onError((e) -> {
            log.error("스케쥴 SSE 에러 발생: ", e);
            scheduleContainer.stop();
            userEmitters.remove(userName);
        });

        emitter.onTimeout(() -> {
            log.info(userName + " 에서 스케쥴 푸시 타임아웃 발생");
            emitter.complete();
            scheduleContainer.stop();
            userEmitters.remove(userName);
        });
    }

    private SimpleMessageListenerContainer createMessageListenerContainer(String queueName) {
        SimpleMessageListenerContainer scheduleContainer = new SimpleMessageListenerContainer(connectionFactory);
        scheduleContainer.setQueueNames(queueName);
        log.info("스케쥴 컨테이너 생성: "+scheduleContainer);
        return scheduleContainer;
    }

    private void sendMailIfEmailIsNotEmpty(String email, String payload) throws MailSendException, ParseException {
        if (email != null) {
            try {
                mailSendService.sendMail(email, payload);
            }catch(Exception e){
                log.error("스케쥴 메일 발송 오류!");
            }
            log.info(email + "로 스케쥴 " + payload + "전송!");
        }
    }

    private void handleIOException(SimpleMessageListenerContainer container, Exception e) {
        if (e.getCause() instanceof ClientAbortException) {
            log.error("비정상적 스케쥴링 클라이언트 종료!");
            container.stop();
        } else {
            log.error(e.getMessage());
            throw new EmitterException("스케쥴링 sse 이벤트 메시지 생성 오류!");
        }
    }
}
