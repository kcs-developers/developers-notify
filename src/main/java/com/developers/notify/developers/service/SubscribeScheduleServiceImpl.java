package com.developers.notify.developers.service;

import com.developers.notify.developers.entity.SubscriptionSchedule;
import com.developers.notify.developers.repository.UserSubscribeScheduleRepository;
import lombok.AllArgsConstructor;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class SubscribeScheduleServiceImpl implements SubscribeScheduleService{
    private final RabbitTemplate rabbitTemplate; // rabbitmq 서버로 메시지 전달
    private final RabbitAdmin rabbitAdmin; // rabbitmq 큐, exchange, router 연결 설정
    private final SchedulerService schedulerService; // 클라이언트로 메시지 전달

    private final UserSubscribeScheduleRepository userSubscribeScheduleRepository; // 구독 정보(스케쥴링) 저장 DB 메소드

    @Override
    public void mentorPublishMessage(String mentorName) {
        // 모든 동적 문자열에는 push.schedule 로 추가
        String exchangeStr = "push.schedule.exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);

        List<String> userNames = getUserList(mentorName);
        // 사용자 큐에 메시지 발송
        for(String userName : userNames){
            String queStr = "push.schedule."+mentorName+"."+userName;
            Queue queue = new Queue(queStr, true, false, false);
            rabbitAdmin.declareQueue(queue);

            String routeStr = "push.shcedule."+mentorName+"."+userName;

            Binding binding = BindingBuilder.bind(queue).to(exchange).with(routeStr).noargs();
            rabbitAdmin.declareBinding(binding);

            String message = "**멘토링 시작 10분 전입니다**";
            rabbitTemplate.convertAndSend(exchangeStr, routeStr, message);
        }

    }

    @Override
    public SseEmitter subscribeMentorAndMessage(String mentorName, String userName, String time) {
        String queStr = "push.schedule."+mentorName;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime notificationTime = LocalDateTime.parse(time, formatter);
        SseEmitter emitter = new SseEmitter(-1L);

        schedulerService.scheduleNotify(notificationTime, queStr, userName, emitter);

        // 구독 정보 DB에 저장
        saveSubscription(userName, mentorName);
        return emitter;
    }

    @Override
    public void unsubscribeMentor(String mentorName, String userName) {
        String queStr = "push.schedule."+mentorName;
        rabbitAdmin.deleteQueue(queStr);
        deleteSubscription(userName, mentorName);
    }

    @Override
    public List<SubscriptionSchedule> getAllSubscriptions(String userName) {
        return userSubscribeScheduleRepository.findAllByUserName(userName);
    }

    public List<String> getUserList(String mentorName){
        List<SubscriptionSchedule> subscriptionSchedules = userSubscribeScheduleRepository.findAllByMentorName(mentorName);
        return subscriptionSchedules.stream().map(SubscriptionSchedule::getUserName).collect(Collectors.toList());
    }

    @Override
    public void saveSubscription(String userName, String mentorName){
        SubscriptionSchedule existingSubscription = userSubscribeScheduleRepository.findByUserNameAndMentorName(userName, mentorName);
        if(existingSubscription == null){
            SubscriptionSchedule subscriptionSchedule = new SubscriptionSchedule(userName, mentorName);
            userSubscribeScheduleRepository.save(subscriptionSchedule);
        }
    }

    @Override
    public void deleteSubscription(String userName, String mentorName){
        SubscriptionSchedule existingSubscription = userSubscribeScheduleRepository.findByUserNameAndMentorName(userName, mentorName);
        if(existingSubscription != null){
            userSubscribeScheduleRepository.delete(existingSubscription);
        }
    }

}