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
public class OTRecoveryRoomBillingRequest {

    private Long operationExternalId;

    private Long wardRoomId;
    private Long wardRoomBedId;
    private String wardRoomName;

    private LocalDateTime startTime;
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;
}