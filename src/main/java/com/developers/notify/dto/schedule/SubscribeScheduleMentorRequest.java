package com.developers.notify.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SubscribeScheduleMentorRequest {
    private String mentorName;
    private String userName;
    private String email;
}
