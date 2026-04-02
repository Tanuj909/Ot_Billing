package com.billing.ot.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTRoomBilling;

@Repository
public interface OTRoomBillingRepository extends JpaRepository<OTRoomBilling, Long> {
    List<OTRoomBilling> findAllByOtBillingDetails(OTBillingDetails otBillingDetails);

    // endTime null wala — current active room
    Optional<OTRoomBilling> findByOtBillingDetailsAndEndTimeIsNull(
            OTBillingDetails otBillingDetails);
}