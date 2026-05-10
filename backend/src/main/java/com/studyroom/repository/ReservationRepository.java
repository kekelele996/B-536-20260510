package com.studyroom.repository;

import com.studyroom.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);
    List<Reservation> findByTimeSlotId(Long timeSlotId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.timeSlotId = ?1 AND r.status = 'ACTIVE'")
    long countActiveReservationsByTimeSlotId(Long timeSlotId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = ?1 AND r.timeSlotId = ?2 AND r.status = 'ACTIVE'")
    long countActiveReservationsByUserAndTimeSlot(Long userId, Long timeSlotId);
    
    @Query("SELECT r FROM Reservation r WHERE r.timeSlotId = ?1 AND r.status = 'WAITLIST' ORDER BY r.createdAt ASC")
    List<Reservation> findWaitlistByTimeSlotId(Long timeSlotId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.timeSlotId = ?1 AND r.status = 'WAITLIST'")
    long countWaitlistByTimeSlotId(Long timeSlotId);
    
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.userId = ?1 AND r.timeSlotId = ?2 AND r.status = 'WAITLIST'")
    long countWaitlistByUserAndTimeSlot(Long userId, Long timeSlotId);
    
    @Query("SELECT r FROM Reservation r WHERE r.userId = ?1 AND r.timeSlotId = ?2 AND r.status IN ('ACTIVE', 'WAITLIST')")
    List<Reservation> findActiveOrWaitlistByUserAndTimeSlot(Long userId, Long timeSlotId);
    
    @Query("SELECT COALESCE(COUNT(r), 0) + 1 FROM Reservation r WHERE r.timeSlotId = ?1 AND r.status = 'WAITLIST' AND r.createdAt < (SELECT r2.createdAt FROM Reservation r2 WHERE r2.id = ?2)")
    long getWaitlistPosition(Long timeSlotId, Long reservationId);
}
