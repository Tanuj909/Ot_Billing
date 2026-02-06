package com.billing.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.billing.enums.PaymentStatus;
import com.billing.model.BillingMaster;

public interface BillingMasterRepository extends JpaRepository<BillingMaster, Long> {
	
	//For IPD
	Optional<BillingMaster> findByAdmissionId(Long admissionId);
	
	//For OPD
	Optional<BillingMaster> findByAppointmentId(Long appointmentId);

	boolean existsByAppointmentId(Long appointmentId);
	
	@Query("""
		    SELECT
		        bm.labStoreId,
		        COALESCE(SUM(bm.totalAmount), 0),
		        COUNT(bm.id)
		    FROM BillingMaster bm
		    WHERE bm.labStoreId IN :storeIds
		    AND bm.paymentStatus='PAID'
		      AND bm.billingDate BETWEEN :startDate AND :endDate
		      AND bm.moduleType = 'LAB'
		    GROUP BY bm.labStoreId
		""")
		List<Object[]> getStoreWiseRevenue(
		        @Param("storeIds") List<Long> storeIds,
		        @Param("startDate") LocalDateTime startDate,
		        @Param("endDate") LocalDateTime endDate
		);
		
		Optional<BillingMaster> findActiveByLabOrderId(Long labOrderId);
		
		Optional<BillingMaster> findByLabOrderIdAndPaymentStatus(
		        Long labOrderId,
		        PaymentStatus paymentStatus
		);


	
}
