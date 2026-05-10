package com.studyroom.service;

import com.studyroom.dto.TimeSlotVO;
import com.studyroom.entity.StudyRoom;
import com.studyroom.entity.TimeSlot;
import com.studyroom.exception.BusinessException;
import com.studyroom.repository.StudyRoomRepository;
import com.studyroom.repository.TimeSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final StudyRoomRepository studyRoomRepository;

    public List<TimeSlotVO> getTimeSlotsByStudyRoomAndDate(Long studyRoomId, LocalDate date) {
        List<TimeSlot> timeSlots = timeSlotRepository.findByStudyRoomIdAndDate(studyRoomId, date);
        return timeSlots.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public List<TimeSlotVO> getTimeSlotsByStudyRoom(Long studyRoomId) {
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(7);
        List<TimeSlot> timeSlots = timeSlotRepository.findByStudyRoomIdAndDateBetween(
                studyRoomId, today, endDate);
        return timeSlots.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    public TimeSlot getTimeSlotById(Long id) {
        return timeSlotRepository.findById(id)
                .orElseThrow(() -> new BusinessException("时间段不存在"));
    }

    @Transactional
    public TimeSlot createTimeSlot(TimeSlot timeSlot) {
        return timeSlotRepository.save(timeSlot);
    }

    @Transactional
    public void deleteTimeSlot(Long id) {
        timeSlotRepository.deleteById(id);
    }

    private TimeSlotVO convertToVO(TimeSlot timeSlot) {
        TimeSlotVO vo = new TimeSlotVO();
        vo.setId(timeSlot.getId());
        vo.setStudyRoomId(timeSlot.getStudyRoomId());
        vo.setDate(timeSlot.getDate());
        vo.setStartTime(timeSlot.getStartTime());
        vo.setEndTime(timeSlot.getEndTime());
        vo.setAvailableSeats(timeSlot.getAvailableSeats());
        vo.setStatus(timeSlot.getStatus());
        
        studyRoomRepository.findById(timeSlot.getStudyRoomId())
                .ifPresent(room -> vo.setStudyRoomName(room.getName()));
        
        return vo;
    }
}
