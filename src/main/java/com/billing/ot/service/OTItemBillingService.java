package com.billing.ot.service;

import java.util.List;

import com.billing.ot.dto.OTItemBillingRequest;
import com.billing.ot.dto.OTItemBillingResponse;
import com.billing.ot.dto.OTItemBillingSummaryResponse;
import com.billing.ot.dto.OTItemBillingUpdateRequest;
import com.billing.ot.enums.OTItemType;

public interface OTItemBillingService {

	OTItemBillingResponse addItem(OTItemBillingRequest request);

	OTItemBillingResponse updateItem(Long itemBillingId, OTItemBillingUpdateRequest request);

	void removeItem(Long itemBillingId);

	List<OTItemBillingResponse> getItemsByOperationId(Long operationId);

	List<OTItemBillingResponse> getItemsByType(Long operationId, OTItemType itemType);

	OTItemBillingSummaryResponse getItemSummary(Long operationId);

}
