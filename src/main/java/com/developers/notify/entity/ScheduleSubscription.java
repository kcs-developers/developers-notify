package com.developers.notify.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@Getter
@ToString
public class ScheduleSubscription {
    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long subscribeId;

    @NonNull
    @Column(nullable = false)
    private String userName;

    @NonNull
    @Column(nullable = false)
    private String mentorName;

    @NonNull
    @Column(nullable = false)
    private String roomName;

    @Column(nullable = true)
    private LocalDateTime startTime;

    @Builder
    public ScheduleSubscription(String userName, String mentorName, String roomName, LocalDateTime startTime){
        this.userName = userName;
        this.mentorName = mentorName;
        this.roomName = roomName;
        this.startTime = startTime;
    }
}
