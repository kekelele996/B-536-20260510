package com.studyroom.repository;

import com.studyroom.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    List<Waitlist> findByTimeSlotIdAndStatusOrderByCreatedAtAsc(Long timeSlotId, String status);

    List<Waitlist> findByUserId(Long userId);

    Optional<Waitlist> findByUserIdAndTimeSlotIdAndStatus(Long userId, Long timeSlotId, String status);

    long countByTimeSlotIdAndStatus(Long timeSlotId, String status);

    @Query("SELECT COALESCE(MAX(w.queuePosition), 0) FROM Waitlist w WHERE w.timeSlotId = ?1 AND w.status = 'WAITING'")
    Integer findMaxQueuePositionByTimeSlotId(Long timeSlotId);

    List<Waitlist> findByTimeSlotIdAndStatusAndQueuePositionGreaterThanOrderByQueuePositionAsc(Long timeSlotId, String status, Integer queuePosition);
}
