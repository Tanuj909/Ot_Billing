package com.billing.ot.service;

import com.billing.ot.dto.OTRecoveryRoomBillingRequest;
import com.billing.ot.dto.OTRecoveryRoomBillingResponse;
import com.billing.ot.dto.OTRecoveryRoomBillingUpdateRequest;
import com.billing.ot.dto.OTRecoveryRoomBillingEndRequest;

import java.util.List;

public interface OTRecoveryRoomBillingService {

    OTRecoveryRoomBillingResponse createRecoveryRoom(OTRecoveryRoomBillingRequest request);

    OTRecoveryRoomBillingResponse updateRecoveryRoom(Long recoveryId, OTRecoveryRoomBillingUpdateRequest request);

    OTRecoveryRoomBillingResponse setEndTime(OTRecoveryRoomBillingEndRequest request);

    void removeRecoveryRoom(Long recoveryId);

    OTRecoveryRoomBillingResponse getById(Long recoveryId);

    List<OTRecoveryRoomBillingResponse> getByOperationId(Long operationId);

    OTRecoveryRoomBillingResponse getCurrentRecoveryRoom(Long operationId);
}