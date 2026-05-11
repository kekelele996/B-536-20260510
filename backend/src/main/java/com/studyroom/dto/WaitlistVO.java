package com.studyroom.dto;

import lombok.Data;
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
    private String date;
    private String startTime;
    private String endTime;
    private Integer queuePosition;
    private String status;
    private LocalDateTime createdAt;
}
