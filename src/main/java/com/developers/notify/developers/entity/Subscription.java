package com.developers.notify.developers.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@NoArgsConstructor
@Getter
public class Subscription {

    @NonNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, name = "subscribe_id")
    private Long subscribe_id;

    @NonNull
    @Column(nullable = false, name = "user_id")
    private Long user_id;

    @NonNull
    @Column(nullable = false, name = "mentor_name")
    private String mentor_name;

    @Builder
    public Subscription(Long user_id, String mentor_name){
        this.user_id = user_id;
        this.mentor_name = mentor_name;
    }
}
