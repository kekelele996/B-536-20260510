package com.studyroom.dto;

import lombok.Data;

@Data
public class WaitlistRequest {
    private Long userId;
    private Long timeSlotId;
}
