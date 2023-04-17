package com.developers.notify.service;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

@Service
@AllArgsConstructor
@Log4j2
public class MailSendService {
    private JavaMailSender javaMailSender;

    public void sendMail(String email, String message) throws ParseException, MailSendException {
        // 전달하는 메시지 형식을 제한하여, 필요에 따라 전송되는 내용의 차이를 두는 방법이 좋을 듯
        String[] sendText = message.split("::");

        LocalDateTime now = LocalDateTime.now();
        String today = now.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일 HH시 mm분 ss초"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일 HH시 mm분 ss");
        Date date = format.parse(today);

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("developers.notify.only@gmail.com");
        mailMessage.setTo(email);
        if(sendText[0] == "push"){
            mailMessage.setSubject("[Developers] 구독하신 멘토의 새 소식 발송!");
            mailMessage.setText(sendText[1]);
        }else if(sendText[0] == "schedule"){
            mailMessage.setSubject("[Developers] 멘토링이 곧 시작합니다!");
            mailMessage.setText(sendText[1]);
        }
        mailMessage.setSentDate(date);
        try {
            log.info(mailMessage);
            javaMailSender.send(mailMessage);
        }catch (MailSendException e){
            log.error("메시지 전송 오류! ", e.getMessage());
            throw new MailSendException(e.getFailedMessages());
        }
    }
}
