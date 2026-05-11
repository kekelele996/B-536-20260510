package com.studyroom.repository;

import com.studyroom.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    List<Waitlist> findByUserId(Long userId);

    List<Waitlist> findByTimeSlotIdOrderByPositionAsc(Long timeSlotId);

    @Query("SELECT w FROM Waitlist w WHERE w.timeSlotId = ?1 AND w.status = 'WAITING' ORDER BY w.position ASC")
    List<Waitlist> findWaitingByTimeSlotId(Long timeSlotId);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.timeSlotId = ?1 AND w.status = 'WAITING'")
    long countWaitingByTimeSlotId(Long timeSlotId);

    @Query("SELECT COUNT(w) FROM Waitlist w WHERE w.userId = ?1 AND w.timeSlotId = ?2 AND w.status = 'WAITING'")
    long countWaitingByUserAndTimeSlot(Long userId, Long timeSlotId);

    Optional<Waitlist> findByUserIdAndTimeSlotIdAndStatus(Long userId, Long timeSlotId, String status);
}
