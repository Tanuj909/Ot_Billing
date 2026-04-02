package com.billing.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTItemBilling;
import com.billing.ot.enums.OTItemType;

public interface OTItemBillingRepository extends JpaRepository<OTItemBilling, Long> {
    List<OTItemBilling> findByOtBillingDetails(OTBillingDetails otBillingDetails);
    List<OTItemBilling> findByOtBillingDetailsAndItemType(
            OTBillingDetails otBillingDetails, OTItemType itemType);
    boolean existsByOtBillingDetailsAndItemExternalId(
            OTBillingDetails otBillingDetails, Long itemExternalId);
}