package com.developers.notify.developers.entity;

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

    @Builder
    public Subscription(String userName, String mentorName){
        this.userName = userName;
        this.mentorName = mentorName;
    }
}
