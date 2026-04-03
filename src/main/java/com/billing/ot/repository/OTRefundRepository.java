package com.billing.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTPayment;
import com.billing.ot.entity.OTRefund;

public interface OTRefundRepository extends JpaRepository<OTRefund, Long> {
    List<OTRefund> findByOtBillingDetails(OTBillingDetails otBillingDetails);
    List<OTRefund> findByOtPayment(OTPayment otPayment);
}
