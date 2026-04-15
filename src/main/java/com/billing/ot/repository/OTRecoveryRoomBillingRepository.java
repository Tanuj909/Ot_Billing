package com.billing.ot.repository;

import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTRecoveryRoomBilling;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OTRecoveryRoomBillingRepository extends JpaRepository<OTRecoveryRoomBilling, Long> {

    Optional<OTRecoveryRoomBilling> findByOtBillingDetails(OTBillingDetails otBillingDetails);

    List<OTRecoveryRoomBilling> findByOtBillingDetails_Id(Long otBillingDetailsId);

    boolean existsByOtBillingDetails(OTBillingDetails otBillingDetails);
}