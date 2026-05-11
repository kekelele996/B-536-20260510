package com.studyroom.dto;

import lombok.Data;

@Data
public class ReservationRequest {
    private Long userId;
    private Long timeSlotId;
}
