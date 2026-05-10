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
}
