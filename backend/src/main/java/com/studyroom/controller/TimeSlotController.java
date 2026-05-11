package com.studyroom.controller;

import com.studyroom.common.Result;
import com.studyroom.dto.TimeSlotVO;
import com.studyroom.entity.TimeSlot;
import com.studyroom.service.TimeSlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {

    private final TimeSlotService timeSlotService;

    @GetMapping("/study-room/{studyRoomId}")
    public Result<List<TimeSlotVO>> getTimeSlotsByStudyRoom(@PathVariable Long studyRoomId,
                                                             @RequestParam(required = false) 
                                                             @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
                                                             LocalDate date) {
        List<TimeSlotVO> timeSlots;
        if (date != null) {
            timeSlots = timeSlotService.getTimeSlotsByStudyRoomAndDate(studyRoomId, date);
        } else {
            timeSlots = timeSlotService.getTimeSlotsByStudyRoom(studyRoomId);
        }
        return Result.success(timeSlots);
    }

    @PostMapping
    public Result<TimeSlot> createTimeSlot(@RequestBody TimeSlot timeSlot) {
        TimeSlot created = timeSlotService.createTimeSlot(timeSlot);
        return Result.success("创建成功", created);
    }

    @DeleteMapping("/{id}")
    public Result<?> deleteTimeSlot(@PathVariable Long id) {
        timeSlotService.deleteTimeSlot(id);
        return Result.success("删除成功", null);
    }
}
