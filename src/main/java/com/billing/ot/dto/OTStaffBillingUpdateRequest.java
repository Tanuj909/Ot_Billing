package com.billing.ot.dto;

import lombok.Data;

//Update Request
@Data
public class OTStaffBillingUpdateRequest {
 private Double fees;
 private Double discountPercent;
 private Double gstPercent;
}