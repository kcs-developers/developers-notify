package com.developers.notify.dto.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DeleteScheduleMentorRequest {
    private String mentorName;
    private String userName;
    private String roomName;
}
