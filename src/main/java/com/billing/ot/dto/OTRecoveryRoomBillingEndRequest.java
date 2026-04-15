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
public class OTRecoveryRoomBillingEndRequest {

    private Long operationExternalId;
    private LocalDateTime endTime;
}