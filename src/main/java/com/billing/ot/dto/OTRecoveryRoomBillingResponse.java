package com.billing.ot.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTRecoveryRoomBillingResponse {

    private Long id;
    private Long otBillingDetailsId;
    private Long operationExternalId;

    private Long wardRoomId;
    private Long wardRoomBedId;
    private String wardRoomName;

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

    private Boolean isCurrent;   // endTime null hai toh true

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}