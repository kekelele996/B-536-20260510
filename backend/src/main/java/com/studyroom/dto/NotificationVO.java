package com.studyroom.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Long relatedReservationId;
    private String type;
    private String status;
    private LocalDateTime createdAt;
}
