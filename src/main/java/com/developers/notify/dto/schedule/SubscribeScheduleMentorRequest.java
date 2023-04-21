package com.developers.notify.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SubscribeScheduleMentorRequest {
    private String mentorName;
    private String userName;
    private String email;
    private String roomName;
    private LocalDateTime startTime;
}
