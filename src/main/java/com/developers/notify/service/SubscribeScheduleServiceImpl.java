package com.developers.notify.service;

import com.developers.notify.dto.schedule.PublishScheduleMentorRequest;
import com.developers.notify.entity.SubscriptionSchedule;
import com.developers.notify.repository.UserSubscribeScheduleRepository;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.QueuesNotAvailableException;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import org.yaml.snakeyaml.emitter.EmitterException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Log4j2
public class SubscribeScheduleServiceImpl implements SubscribeScheduleService{
    private final RabbitTemplate rabbitTemplate; // rabbitmq 서버로 메시지 전달
    private final RabbitAdmin rabbitAdmin; // rabbitmq 큐, exchange, router 연결 설정
    private final SchedulerService schedulerService; // 클라이언트로 메시지 전달

    private final UserSubscribeScheduleRepository userSubscribeScheduleRepository; // 구독 정보(스케쥴링) 저장 DB 메소드

    @Override
    public void mentorPublishMessage(PublishScheduleMentorRequest request) throws Exception {
        // 모든 동적 문자열에는 push.schedule 로 추가
        String exchangeStr = "push.schedule.exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);

        List<String> userNames = getUserList(request.getMentorName());
        log.info(request.getMentorName()+"의 구독 목록: "+userNames);
        // 사용자 큐에 메시지 발송
        try {
            for (String userName : userNames) {
                String routeStr = "push.schedule.route" + request.getMentorName() + "." + userName;
                String message = "**멘토링 시작 10분 전입니다**";
                rabbitTemplate.convertAndSend(exchangeStr, routeStr, message);
            }
        }catch (Exception e){
            log.error("메시지 발행 오류! ");
            throw new Exception("메시지 발행이 실패하였습니다");
        }
    }

    @Override
    public void subscribeMentor(String mentorName, String userName, String email) throws Exception {
        // 멘토+사용자 로 큐 생성
        String queStr = "push.schedule.queue" + mentorName + "." + userName;

        // 문자열 큐 생성
        Queue queue = new Queue(queStr, true, false, false);
        rabbitAdmin.declareQueue(queue);

        // 문자열 익스체인지 생성
        String exchangeStr = "push.schedule.exchange";
        Exchange exchange = ExchangeBuilder.topicExchange(exchangeStr).build();
        rabbitAdmin.declareExchange(exchange);

        // 문자열 키 생성
        String routeStr = "push.schedule.route" + mentorName + "." + userName;

        // Queue, Exchange 바인딩
        try {
            Binding binding = BindingBuilder.bind(queue).to(exchange).with(routeStr).noargs();
            rabbitAdmin.declareBinding(binding);
            log.info(binding + " 이 생성되었습니다!");
        }catch (Exception e){
            log.error("메시지 큐 바인딩 실패");
            throw new QueuesNotAvailableException("큐 바인딩 실패! ",e.getCause());
        }
        // 구독 정보 DB에 저장
        try {
            saveSubscription(userName, mentorName);
            log.info(userName+"에" + mentorName + "이 저장되었습니다");
        }catch (Exception e){
            log.error("DB 저장 오류");
            throw new Exception("구독 목록 저장이 실패하였습니다");
        }
    }

    @Override
    public SseEmitter listenSchedulePush(String mentorName, String userName, String time,String email){
        // 멘토+사용자 로 큐 생성
        String queStr = "push.schedule.queue" + mentorName + "." + userName;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        LocalDateTime notificationTime = LocalDateTime.parse(time, formatter);

        SseEmitter emitter = new SseEmitter(-1L);

        try {
            schedulerService.scheduleNotify(notificationTime, queStr, userName, emitter, email);
        }catch (Exception e){
            log.error("알림 스케쥴링 실패!");
            throw new EmitterException("알림이 실패하였습니다! "+e.getMessage());
        }

        return emitter;
    }

    @Override
    public void unsubscribeMentor(String mentorName, String userName) throws Exception {
        String queStr = "push.schedule.queue"+mentorName+userName;
        try {
            rabbitAdmin.deleteQueue(queStr);
            log.info("큐 삭제 완료");
        }catch (Exception e){
            log.error("큐 삭제 실패! ");
            throw new QueuesNotAvailableException("큐 삭제가 실패하였습니다", e.getCause());
        }
        try {
            deleteSubscription(userName, mentorName);
            log.info("DB 삭제 완료!");
        }catch (Exception e){
            log.error("DB 삭제 실패!");
            throw new Exception(mentorName+"큐에 대한 삭제가 실패하였습니다");
        }
    }

    @Override
    public List<SubscriptionSchedule> getAllSubscriptions(String userName) {
        log.info(userSubscribeScheduleRepository.findAllByUserName(userName));
        return userSubscribeScheduleRepository.findAllByUserName(userName);
    }

    public List<String> getUserList(String mentorName){
        List<SubscriptionSchedule> subscriptionSchedules = userSubscribeScheduleRepository.findAllByMentorName(mentorName);
        log.info(subscriptionSchedules);
        return subscriptionSchedules.stream().map(SubscriptionSchedule::getUserName).collect(Collectors.toList());
    }

    @Override
    public void saveSubscription(String userName, String mentorName){
        SubscriptionSchedule existingSubscription = userSubscribeScheduleRepository.findByUserNameAndMentorName(userName, mentorName);
        if(existingSubscription == null){
            SubscriptionSchedule subscriptionSchedule = new SubscriptionSchedule(userName, mentorName);
            log.info(subscriptionSchedule);
            userSubscribeScheduleRepository.save(subscriptionSchedule);
        }else{
            log.error("중복 저장 오류");
        }
    }

    @Override
    public void deleteSubscription(String userName, String mentorName){
        SubscriptionSchedule existingSubscription = userSubscribeScheduleRepository.findByUserNameAndMentorName(userName, mentorName);
        if(existingSubscription != null){
            userSubscribeScheduleRepository.delete(existingSubscription);
            log.info("삭제 요청");
        }else{
            log.error("삭제 요청 실패");
        }
    }

}