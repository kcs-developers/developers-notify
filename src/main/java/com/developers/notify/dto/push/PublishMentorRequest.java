package com.developers.notify.dto.push;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PublishMentorRequest {
    private String message;
    private String mentorName;
}
