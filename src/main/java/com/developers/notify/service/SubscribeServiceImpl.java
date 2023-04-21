package com.developers.notify.service;

import com.developers.notify.dto.push.PublishMentorRequest;
import com.developers.notify.entity.Subscription;
import com.developers.notify.repository.UserSubscribeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.QueuesNotAvailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class SubscribeServiceImpl implements SubscribeService{
    private final RabbitTemplate rabbitTemplate; // rabbitmq 서버로 메시지 전달
    private final RabbitAdmin rabbitAdmin; // rabbitmq 큐, exchange, router 연결 설정
    private final MessageService messageService; // 클라이언트로 메시지 전달
    private final UserSubscribeRepository userSubscribeRepository; // 구독 정보 저장 DB 메소드

    @Override
    public void mentorPublishMessage(PublishMentorRequest request) throws Exception {
        // 문자열 익스체인지 생성
        String exchangeStr = "push.exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);

        List<String> userNames = getUserList(request.getMentorName());
        log.info(request.getMentorName()+"의 구독 목록: "+userNames);
        // 라우팅 키 생성
        try {
            for (String userName : userNames) {
                String routeStr = "push.route." + request.getMentorName() + "." + userName;
                // 메시지 전송
                log.info("큐로 메시지 전송 성공!");
                rabbitTemplate.convertAndSend(exchangeStr, routeStr, request.getMessage());
            }
        }catch (Exception e){
            log.error("메시지 발행 오류! ");
            throw new Exception("메시지 발행이 실패하였습니다");
        }
    }

    @Override
    public List<Subscription> subscribeMentor(String mentorName, String userName, String email) throws Exception {
            // 멘토+사용자 로 큐 생성
            String queStr = "push.queue." + mentorName + "." + userName;

            // 기존 큐 확인
            Properties queueProperties = rabbitAdmin.getQueueProperties(queStr);

            // 기존 큐가 없으면 큐 생성 및 바인딩
            if (queueProperties == null || queueProperties.isEmpty()) {
                try {
                    // 문자열 큐 생성
                    Queue queue = new Queue(queStr, true, false, false);
                    rabbitAdmin.declareQueue(queue);

                    // 문자열 익스체인지 생성
                    String exchangeStr = "push.exchange";
                    Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
                    rabbitAdmin.declareExchange(exchange);

                    // 문자열 키 생성
                    String routeStr = "push.route." + mentorName + "." + userName;

                    // Queue, Exchange 바인딩
                    Binding binding = BindingBuilder.bind(queue).to(exchange).with(routeStr).noargs();
                    rabbitAdmin.declareBinding(binding);
                    log.info(binding + " 이 생성되었습니다!");
                } catch (Exception e) {
                    log.error("메시지 큐 생성 오류! " + e);
                    throw new QueuesNotAvailableException("큐 바인딩 실패! ", e.getCause());
                }
            }
            try {
                saveSubscription(userName, mentorName);
                log.info(userName + "에" + mentorName + "이 저장되었습니다");
            } catch (Exception e) {
                log.error("DB 저장 오류");
                throw new Exception("구독 목록 저장이 실패하였습니다");
            }
            return userSubscribeRepository.findAllByUserName(userName);
        }

    @Override
    public SseEmitter listenPush(String mentorName, String userName, String email){
        // 멘토+사용자 로 큐 생성
        String queStr = "push.queue." + mentorName + "." + userName;

        // SSE 전달 객체 생성
        SseEmitter emitter = new SseEmitter(3 * 60 * 60 * 1000L);

        // 생성된 개인 사용자의 메시지 큐에 구독하고, 추후 메시지 발생시 반환
        try {
            messageService.subscribeToMessages(queStr, userName, emitter, email);
            log.info("메시지 수신 대기");
        }catch (Exception e){
            log.error("알림 실패!");
            throw new EmitterException("알림이 실패하였습니다! "+e.getMessage());
        }

        return emitter;
    }

    @Override
    public List<Subscription> unsubscribeMentor(String mentorName, String userName) throws Exception {
        String queStr = "push.queue."+mentorName+"."+userName;
        try {
            rabbitAdmin.deleteQueue(queStr);
            log.info("큐 삭제 완료");
        }catch(Exception e){
            log.error("큐 삭제 실패");
            throw new QueuesNotAvailableException("큐 삭제가 실패하였습니다", e.getCause());
        }
        try {
            deleteSubscription(userName, mentorName);
            log.info("DB 삭제 완료");
        }catch (Exception e){
            log.error("DB 삭제 실패");
            throw new Exception(mentorName+"큐에 대한 삭제가 실패하였습니다");
        }
        return userSubscribeRepository.findAllByUserName(userName);
    }
    @Override
    public List<Subscription> getAllSubscriptions(String userName) {
        log.info(userSubscribeRepository.findAllByUserName(userName));
        return userSubscribeRepository.findAllByUserName(userName);
    }

    @Override
    public List<String> getUserList(String mentorName) {
        List<Subscription> subscriptions = userSubscribeRepository.findAllByMentorName(mentorName);
        log.info(subscriptions);
        return subscriptions.stream().map(Subscription::getUserName).collect(Collectors.toList());
    }

    @Override
    public void saveSubscription(String userName, String mentorName) {
        // 중복 저장 방지
        Subscription existingSubscription = userSubscribeRepository.findByUserNameAndMentorName(userName, mentorName);
        if (existingSubscription == null) {
            Subscription subscription = new Subscription(userName, mentorName);
            userSubscribeRepository.save(subscription);
        }else{
            log.error("중복 저장 오류");
        }
    }

    @Override
    public void deleteSubscription(String userName, String mentorName) {
        // 삭제 조건 확인
        Subscription existingSubscription = userSubscribeRepository.findByUserNameAndMentorName(userName, mentorName);
        if (existingSubscription != null) {
            userSubscribeRepository.delete(existingSubscription);
            log.info("삭제 요청");
        }else{
            log.error("삭제 요청 실패");
        }
    }
}
