package com.developers.notify.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
@ToString
public class Subscription {

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

    private String roomName;
    private String startTime;

    @Builder
    public Subscription(String userName, String mentorName, String roomName, String startTime){
        this.userName = userName;
        this.mentorName = mentorName;
        this.roomName = roomName;
        this.startTime = startTime;
    }
}
