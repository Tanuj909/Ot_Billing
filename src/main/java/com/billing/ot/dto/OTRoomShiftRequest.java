package com.billing.ot.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OTRoomShiftRequest {
    private Long operationExternalId;
    private LocalDateTime shiftTime;
    private String newRoomNumber;
    private String newRoomName;
    private Double newRatePerHour;
    private Double newDiscountPercent;
    private Double newGstPercent;
}