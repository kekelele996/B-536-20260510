package com.studyroom.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NotificationVO {
    private Long id;
    private Long userId;
    private String title;
    private String content;
    private String type;
    private Long relatedId;
    private String status;
    private LocalDateTime createdAt;
}
