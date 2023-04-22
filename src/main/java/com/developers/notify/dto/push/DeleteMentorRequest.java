package com.developers.notify.dto.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DeleteMentorRequest {
    private String mentorName;
    private String userName;
    private String roomName;
}
