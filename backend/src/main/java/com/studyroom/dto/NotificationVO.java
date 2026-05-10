package com.studyroom.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private Long userId;
    private String message;
    private String type;
    private Boolean isRead;
    private Long relatedId;
    private LocalDateTime createdAt;
}
