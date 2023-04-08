package com.developers.notify.developers.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@NoArgsConstructor
@Getter
public class SubscriptionSchedule {

    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name="subscribe_schedule_id")
    private Long subscribeScheduleId;

    @NonNull
    @Column(nullable = false, name = "user_name")
    private String userName;

    @NonNull
    @Column(nullable = false, name = "mentor_name")
    private String mentorName;

    @Builder
    public SubscriptionSchedule(String userName, String mentorName){
        this.userName = userName;
        this.mentorName = mentorName;
    }
}
