package com.developers.notify.repository;

import com.developers.notify.entity.ScheduleSubscription;
import com.developers.notify.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UserScheduleSubscribeRepository extends JpaRepository<ScheduleSubscription, Long> {
    List<ScheduleSubscription> findAllByUserName(String userName);

    List<ScheduleSubscription> findAllByMentorName(String mentorName);
    ScheduleSubscription findByUserNameAndMentorNameAndRoomName(String userName, String mentorName, String roomName);

    ScheduleSubscription findByUserNameAndMentorNameAndRoomNameAndStartTime(String userName, String mentorName, String roomName, LocalDateTime startTime);

}
