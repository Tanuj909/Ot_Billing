package com.billing.ot.service;

import java.util.List;

import com.billing.ot.dto.OTRoomBillingEndRequest;
import com.billing.ot.dto.OTRoomBillingRequest;
import com.billing.ot.dto.OTRoomBillingResponse;
import com.billing.ot.dto.OTRoomBillingUpdateRequest;
import com.billing.ot.dto.OTRoomShiftRequest;

public interface OTRoomBillingService {

	OTRoomBillingResponse createRoomBilling(OTRoomBillingRequest request);

	OTRoomBillingResponse updateRoomBilling(OTRoomBillingUpdateRequest request);

	OTRoomBillingResponse setEndTime(OTRoomBillingEndRequest request);

	List<OTRoomBillingResponse> shiftRoom(OTRoomShiftRequest request);

	List<OTRoomBillingResponse> getRoomBillingByOperationId(Long operationId);

	OTRoomBillingResponse getCurrentRoom(Long operationId);




}
