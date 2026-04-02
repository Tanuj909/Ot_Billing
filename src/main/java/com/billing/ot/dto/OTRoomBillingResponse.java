package com.billing.ot.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTRoomBillingResponse {
    private Long id;
    private Long otBillingDetailsId;
    private String roomNumber;
    private String roomName;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long durationMinutes;
    private Double totalHours;
    private Double ratePerHour;
    private Double baseAmount;
    private Double discountPercent;
    private Double discountAmount;
    private Double priceAfterDiscount;
    private Double gstPercent;
    private Double gstAmount;
    private Double totalAmount;
    private Boolean isCurrent;      // endTime null = current active room
    private LocalDateTime createdAt;
}
