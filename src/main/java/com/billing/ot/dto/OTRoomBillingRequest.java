package com.billing.ot.dto;


import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OTRoomBillingRequest {
    private Long operationExternalId;
    private String roomNumber;
    private String roomName;
    private LocalDateTime startTime;
    private Double ratePerHour;
    private Double discountPercent;
    private Double gstPercent;
}