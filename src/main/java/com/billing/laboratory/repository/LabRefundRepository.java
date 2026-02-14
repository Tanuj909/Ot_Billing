package com.billing.laboratory.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.billing.enums.RefundStatus;
import com.billing.laboratory.entity.LabRefund;

public interface LabRefundRepository
        extends JpaRepository<LabRefund, Long> {
	
	Optional<LabRefund> findById(Long refundId);
	
	Optional<LabRefund> findByLabOrderId(Long orderId);
	
    @Query("""
            SELECT r FROM LabRefund r
            WHERE r.createdAt BETWEEN :from AND :to
              AND (:labOrderId IS NULL OR r.labOrderId = :labOrderId)
        """)
        List<LabRefund> findRefundReport(
                LocalDateTime from,
                LocalDateTime to,
                Long labOrderId
        );
    
    Optional<List<LabRefund>> getRefundReportByLabStoreId(Long storeId);
    
    boolean existsByBillingIdAndOrderItemIdAndRefundStatus(
            Long billingId,
            Long orderItemId,
            RefundStatus status
    );

    @Query("""
           SELECT COALESCE(SUM(r.refundAmount),0)
           FROM LabRefund r
           WHERE r.billingId = :billingId
           AND r.refundStatus = 'PROCESSED'
           """)
    Double sumProcessedRefundByBillingId(@Param("billingId") Long billingId);

}
