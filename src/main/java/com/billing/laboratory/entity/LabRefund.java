package com.billing.laboratory.entity;

import java.time.LocalDateTime;

import com.billing.enums.PaymentStatus;
import com.billing.enums.RefundStatus;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lab_refund")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long labOrderId;
    private Long orderItemId;
    private Long billingId;
    private Long labStoreId;

    private Double refundAmount;
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 50)
    private RefundStatus refundStatus;

    private LocalDateTime refundedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
