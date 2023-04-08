package com.developers.notify.developers.service;

import com.developers.notify.developers.entity.Subscription;
import com.developers.notify.developers.repository.UserSubscribeRepository;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubscribeServiceImpl implements SubscribeService{
    private final RabbitTemplate rabbitTemplate; // rabbitmq 서버로 메시지 전달
    private final RabbitAdmin rabbitAdmin; // rabbitmq 큐, exchange, router 연결 설정
    private final MessageService messageService; // 클라이언트로 메시지 전달

    private final UserSubscribeRepository userSubscribeRepository; // 구독 정보 저장 DB 메소드

    @Override
    public void mentorPublishMessage(String mentorName, String message) {
        // 문자열 익스체인지 생성
        String exchangeStr = "push.exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);

        // 해당 멘토를 구독하는 모든 사용자의 아이디 목록
        List<String> userIds = getUserList(mentorName);
        // 각 사용자의 큐에 메시지를 전송
        for (String userId : userIds) {
            // 문자열 큐 생성
            String queStr = "push.queue." + mentorName + "." + userId;
            Queue queue = new Queue(queStr, true, false, false);
            rabbitAdmin.declareQueue(queue);
            // 문자열 키 생성
            String routeStr = "push.route." + mentorName + "." + userId;

            // Queue, Exchange 바인딩
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(routeStr).noargs();
            rabbitAdmin.declareBinding(binding);

            rabbitTemplate.convertAndSend(exchangeStr, routeStr, message);
        }
    }

    @Override
    public SseEmitter subscribeMentorAndMessage(String mentorName, String userName) {
        // 멘토+사용자 로 큐 생성
        String queStr = "push.queue."+mentorName+"."+userName;
        // SSE 전달 객체 생성
        SseEmitter emitter = new SseEmitter();
        // 생성된 개인 사용자의 메시지 큐에 구독하고, 추후 메시지 발생시 반환
        //구독 과 메시지 발송 로직 분리?
        messageService.subscribeToMessages(queStr, userName, emitter);

        // 구독 정보 DB에 저장
        saveSubscription(userName, mentorName);
        return emitter;
    }

    @Override
    public void unsubscribeMentor(String mentorName, String userName) {
        String queStr = "push.queue."+mentorName;
        rabbitAdmin.deleteQueue(queStr);
        deleteSubscription(userName, mentorName);
    }
    @Override
    public List<Subscription> getAllSubscriptions(String userName) {
        return userSubscribeRepository.findAllByUserName(userName);
    }

    @Override
    public List<String> getUserList(String mentorName) {
        List<Subscription> subscriptions = userSubscribeRepository.findAllByMentorName(mentorName);
        return subscriptions.stream().map(Subscription::getUserName).collect(Collectors.toList());
    }

    @Override
    public void saveSubscription(String userName, String mentorName) {
        // 중복 저장 방지
        Subscription existingSubscription = userSubscribeRepository.findByUserNameAndMentorName(userName, mentorName);
        if (existingSubscription == null) {
            Subscription subscription = new Subscription(userName, mentorName);
            userSubscribeRepository.save(subscription);
        }
    }

    @Override
    public void deleteSubscription(String userName, String mentorName) {
        // 삭제 조건 확인
        Subscription existingSubscription = userSubscribeRepository.findByUserNameAndMentorName(userName, mentorName);
        if (existingSubscription != null) {
            userSubscribeRepository.delete(existingSubscription);
        }
    }
}
