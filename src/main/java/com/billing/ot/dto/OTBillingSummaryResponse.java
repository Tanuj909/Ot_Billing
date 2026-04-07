package com.billing.ot.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OTBillingSummaryResponse {

    // Operation Info
    private Long operationExternalId;
    private String operationReference;
    private Long patientExternalId;
    private Long hospitalExternalId;

    // Billing Master
    private Long billingMasterId;
    private String paymentStatus;
    private String paymentMode;
    private LocalDateTime billingDate;

    // Charges Breakdown
    private StaffChargesSummary staffCharges;
    private RoomChargesSummary roomCharges;
    private ItemChargesSummary itemCharges;

    // Totals
    private Double grossAmount;
    private Double totalDiscountAmount;
    private Double totalGstAmount;
    private Double totalAmount;
    private Double totalPaid;
    private Double totalRefunded;
    private Double due;

    private String billingStatus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Payment History
    private List<PaymentEntry> payments;
    private List<RefundEntry> refunds;

    // ==================== Nested Classes ==================== //

    @Data
    @Builder
    public static class StaffChargesSummary {
        private Double totalAmount;
        private List<OTStaffBillingResponse> staff;
    }

    @Data
    @Builder
    public static class RoomChargesSummary {
        private Double totalAmount;
        private List<OTRoomBillingResponse> rooms;
    }

    @Data
    @Builder
    public static class ItemChargesSummary {
        private Double totalAmount;
        private Map<String, Double> totalByType;     // IV_FLUID → 500.0
        private List<OTItemBillingResponse> items;
    }

    @Data
    @Builder
    public static class PaymentEntry {
        private Long id;
        private String paymentType;
        private String paymentMode;
        private Double amount;
        private String status;
        private String referenceNumber;
        private String receivedBy;
        private String notes;
        private LocalDateTime paidAt;
    }

    @Data
    @Builder
    public static class RefundEntry {
        private Long id;
        private Long paymentId;
        private Double refundAmount;
        private String reason;
        private String refundMode;
        private String refundStatus;
        private String processedBy;
        private LocalDateTime refundedAt;
    }
}
