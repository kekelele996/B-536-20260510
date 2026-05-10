package com.studyroom.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class TimeSlotVO {
    private Long id;
    private Long studyRoomId;
    private String studyRoomName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer availableSeats;
    private String status;
}
