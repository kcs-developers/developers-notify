package com.developers.notify.developers.repository;

import com.developers.notify.developers.entity.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSubscribeRepository extends JpaRepository<Subscription, Long> {
    List<Subscription> findByUser_id(Long userId);

    List<Subscription> findByMentor_name(String mentorName);
    Subscription findByUserIdAndMentorId(Long userId, String mentorId);
}
