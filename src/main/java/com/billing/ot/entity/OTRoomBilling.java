package com.billing.ot.entity;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//3. OTRoomBilling
@Entity
@Table(name = "ot_room_billing")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OTRoomBilling {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_billing_id", nullable = false)
    private OTBillingDetails otBillingDetails;

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

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        calculateAmounts();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        calculateAmounts();
    }

    public void calculateAmounts() {
        if (ratePerHour == null) return;

        if (startTime != null && endTime != null) {
            this.durationMinutes = ChronoUnit.MINUTES.between(startTime, endTime);
            this.totalHours = durationMinutes / 60.0;
            this.baseAmount = ratePerHour * totalHours;
        } else {
            this.baseAmount = 0.0;
        }

        double effectiveDiscount = discountPercent != null ? discountPercent : 0.0;
        this.discountAmount = baseAmount * effectiveDiscount / 100;
        this.priceAfterDiscount = baseAmount - discountAmount;

        double effectiveGst = gstPercent != null ? gstPercent : 0.0;
        this.gstAmount = priceAfterDiscount * effectiveGst / 100;

        this.totalAmount = priceAfterDiscount + gstAmount;
    }
}