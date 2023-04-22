package com.developers.notify.repository;

import com.developers.notify.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscribeRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findAllByUserName(String userName);

    List<Subscription> findAllByMentorName(String mentorName);
    Subscription findByUserNameAndMentorName(String userName, String mentorName);
    Subscription findByUserNameAndMentorNameAndRoomName(String userName, String mentorName, String roomName);
}
