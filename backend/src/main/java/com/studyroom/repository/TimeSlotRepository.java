package com.studyroom.repository;

import com.studyroom.entity.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TimeSlotRepository extends JpaRepository<TimeSlot, Long> {
    List<TimeSlot> findByStudyRoomIdAndDate(Long studyRoomId, LocalDate date);
    List<TimeSlot> findByStudyRoomIdAndDateBetween(Long studyRoomId, LocalDate startDate, LocalDate endDate);
}
