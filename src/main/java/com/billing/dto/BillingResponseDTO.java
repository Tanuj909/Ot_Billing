package com.billing.dto;

import java.time.LocalDateTime;
import com.billing.enums.PaymentMode;
import com.billing.enums.PaymentStatus;
import lombok.Data;

@Data
public class BillingResponseDTO {
    private Long id;
    private Long hospitaExternallId;
    private Long patientExternalId;
    private Long appointmentId;
    private String moduleType;
    private Double totalAmount;
//    private PaymentStatus paymentStatus;
    private String paymentStatus;
    private PaymentMode paymentMode;
    private LocalDateTime billingDate;
}
