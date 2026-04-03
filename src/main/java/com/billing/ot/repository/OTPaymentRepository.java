package com.billing.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTPayment;
import com.billing.ot.enums.OTPaymentStatus;

public interface OTPaymentRepository extends JpaRepository<OTPayment, Long> {
    List<OTPayment> findByOtBillingDetails(OTBillingDetails otBillingDetails);
    List<OTPayment> findByOtBillingDetailsAndStatus(
            OTBillingDetails otBillingDetails, OTPaymentStatus status);
//    Double sumAmountByOtBillingDetailsAndStatus(
//            OTBillingDetails otBillingDetails, OTPaymentStatus status);
    
    @Query("""
    	    SELECT COALESCE(SUM(p.amount), 0)
    	    FROM OTPayment p
    	    WHERE p.otBillingDetails = :otBillingDetails
    	    AND p.status = :status
    	""")
    	Double sumAmountByOtBillingDetailsAndStatus(
    	        @Param("otBillingDetails") OTBillingDetails otBillingDetails,
    	        @Param("status") OTPaymentStatus status);
}