package com.studyroom.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

@Data
public class WaitlistVO {
    private Long id;
    private Long userId;
    private String username;
    private Long timeSlotId;
    private Long studyRoomId;
    private String studyRoomName;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private Integer position;
    private String status;
    private LocalDateTime createdAt;
}
