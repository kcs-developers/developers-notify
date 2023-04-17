package com.developers.notify.developers.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice //응답을 json로 처리 가능
@Log4j2
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException e){
        log.error("매개변수 오류: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("허용되지 않은 매개변수 값이 입력 되었습니다. "+e.getMessage());
    }
    @ExceptionHandler(MethodArgumentNotValidException.class) //@valid 오류
    public ResponseEntity<?> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        log.error("매개변수 오류: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("유효하지 않은 매개변수 값이 입력 되었습니다. " + e.getBindingResult().getAllErrors().get(0).getDefaultMessage());
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<?> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException e) {
        log.error("API 요청 오류: ", e);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("잘못된 API 요청입니다. " + e.getMessage());
    }

    @ExceptionHandler(Exception.class) //나머지 예외 처리
    public ResponseEntity<?> handleGeneralException(Exception e) {
        log.error("비정상적 예외 발생: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("비정상적인 예외가 발생했습니다. " + e.getMessage());
    }
    // 메일 발송 오류
    @ExceptionHandler(MailSendException.class)
    public ResponseEntity<?> handleMailSendException(MailSendException e){
        log.error("메일 발송 오류: ", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("메일 발송 요류가 발생하였습니다. "+e.getMessage());
    }
}
