package com.billing.ot.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class OTRoomBillingEndRequest {
    private Long operationExternalId;
    private LocalDateTime endTime;
}