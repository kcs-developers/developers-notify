package com.developers.notify.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscribeScheduleRepository extends JpaRepository<SubscriptionSchedule, Long> {
    List<SubscriptionSchedule> findAllByUserName(String userName);

    List<SubscriptionSchedule> findAllByMentorName(String mentorName);
    SubscriptionSchedule findByUserNameAndMentorName(String userName, String mentorName);
}
