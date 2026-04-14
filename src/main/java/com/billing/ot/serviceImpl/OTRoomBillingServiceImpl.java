package com.billing.ot.serviceImpl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.billing.enums.PaymentStatus;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ValidationException;
import com.billing.ot.dto.OTRoomBillingEndRequest;
import com.billing.ot.dto.OTRoomBillingRequest;
import com.billing.ot.dto.OTRoomBillingResponse;
import com.billing.ot.dto.OTRoomBillingUpdateRequest;
import com.billing.ot.dto.OTRoomShiftRequest;
import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTRoomBilling;
import com.billing.ot.repository.OTBillingDetailsRepository;
import com.billing.ot.repository.OTRoomBillingRepository;
import com.billing.ot.service.OTBillingDetailsService;
import com.billing.ot.service.OTRoomBillingService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTRoomBillingServiceImpl implements OTRoomBillingService {

    private final OTRoomBillingRepository roomBillingRepository;
    private final OTBillingDetailsRepository otBillingDetailsRepository;
    private final OTBillingDetailsService otBillingDetailsService;

    // Helper — OTBillingDetails fetch
    private OTBillingDetails getDetails(Long operationId) {
        return otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));
    }

    // Helper — Active room fetch (endTime null)
    private OTRoomBilling getActiveRoom(OTBillingDetails details) {
        return roomBillingRepository
                .findByOtBillingDetailsAndEndTimeIsNull(details)
                .orElseThrow(() -> new ResourceNotFoundException("No Active room found"));
    }

    // Helper — Billing checks
    private void validateBillingActive(OTBillingDetails details) {
        if (!details.getBillingStatus().equals("ACTIVE")) {
            throw new ValidationException("Cannot proceed — billing is "
                    + details.getBillingStatus());
        }
        if (details.getBillingMaster().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Cannot proceed — billing master is CANCELLED");
        }
    }

    // ---------------------------------------- Create ---------------------------------------- //

    @Transactional
    @Override
    public OTRoomBillingResponse createRoomBilling(OTRoomBillingRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());
        validateBillingActive(details);

        // Already Active room check — endTime null wala
        if (roomBillingRepository.findByOtBillingDetailsAndEndTimeIsNull(details).isPresent()) {
            throw new ValidationException(
                    "Active room already exists — use update or shift API");
        }

        OTRoomBilling roomBilling = OTRoomBilling.builder()
                .otBillingDetails(details)
                .roomNumber(request.getRoomNumber())
                .roomName(request.getRoomName())
                .startTime(request.getStartTime())
                .ratePerHour(request.getRatePerHour())
                .discountPercent(request.getDiscountPercent())
                .gstPercent(request.getGstPercent())
                .build();

        roomBillingRepository.save(roomBilling);
        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(roomBilling);
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public OTRoomBillingResponse updateRoomBilling(OTRoomBillingUpdateRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());
        validateBillingActive(details);

        OTRoomBilling ActiveRoom = getActiveRoom(details);

        if (request.getRoomNumber() != null)      ActiveRoom.setRoomNumber(request.getRoomNumber());
        if (request.getRoomName() != null)        ActiveRoom.setRoomName(request.getRoomName());
        if (request.getStartTime() != null)       ActiveRoom.setStartTime(request.getStartTime());
        if (request.getRatePerHour() != null)     ActiveRoom.setRatePerHour(request.getRatePerHour());
        if (request.getDiscountPercent() != null) ActiveRoom.setDiscountPercent(request.getDiscountPercent());
        if (request.getGstPercent() != null)      ActiveRoom.setGstPercent(request.getGstPercent());

        ActiveRoom.calculateAmounts();
        roomBillingRepository.save(ActiveRoom);
        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(ActiveRoom);
    }

    // ---------------------------------------- End Time ---------------------------------------- //

//    @Transactional
//    @Override
//    public OTRoomBillingResponse setEndTime(OTRoomBillingEndRequest request) {
//
//        OTBillingDetails details = getDetails(request.getOperationExternalId());
//        OTRoomBilling ActiveRoom = roomBillingRepository.findByOtBillingDetailsAndEndTimeIsNull(details)
//        		.orElseThrow(()-> new ResourceNotFoundException("Room Not found in Billing!"));
//
//        if (request.getEndTime().isBefore(ActiveRoom.getStartTime())) {
//            throw new ValidationException("End time cannot be before start time");
//        }
//
//        ActiveRoom.setEndTime(request.getEndTime());
//        ActiveRoom.calculateAmounts();
//        roomBillingRepository.save(ActiveRoom);
//        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());
//
//        return mapToResponse(ActiveRoom);
//    }
    
    @Transactional
    @Override
    public OTRoomBillingResponse setEndTime(OTRoomBillingEndRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());

        // 🔥 Simple latest room fetch (NO Active check)
        OTRoomBilling room = roomBillingRepository
                .findByOtBillingDetails_Id(details.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Room Not found in Billing!"));

        if (request.getEndTime().isBefore(room.getStartTime())) {
            throw new ValidationException("End time cannot be before start time");
        }

        room.setEndTime(request.getEndTime());
        room.calculateAmounts();
        roomBillingRepository.save(room);

        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(room);
    }

    // ---------------------------------------- Shift ---------------------------------------- //

    @Transactional
    @Override
    public List<OTRoomBillingResponse> shiftRoom(OTRoomShiftRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());
        validateBillingActive(details);

        OTRoomBilling ActiveRoom = getActiveRoom(details);

        if (request.getShiftTime().isBefore(ActiveRoom.getStartTime())) {
            throw new ValidationException("Shift time cannot be before current room start time");
        }

        // Purana room close karo
        ActiveRoom.setEndTime(request.getShiftTime());
        ActiveRoom.calculateAmounts();
        roomBillingRepository.save(ActiveRoom);

        // Naya room create karo
        OTRoomBilling newRoom = OTRoomBilling.builder()
                .otBillingDetails(details)
                .roomNumber(request.getNewRoomNumber())
                .roomName(request.getNewRoomName())
                .startTime(request.getShiftTime())
                .ratePerHour(request.getNewRatePerHour())
                .discountPercent(request.getNewDiscountPercent())
                .gstPercent(request.getNewGstPercent())
                .build();

        roomBillingRepository.save(newRoom);
        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return roomBillingRepository.findAllByOtBillingDetails(details)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public List<OTRoomBillingResponse> getRoomBillingByOperationId(Long operationId) {
        OTBillingDetails details = getDetails(operationId);
        return roomBillingRepository.findAllByOtBillingDetails(details)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OTRoomBillingResponse getCurrentRoom(Long operationId) {
        OTBillingDetails details = getDetails(operationId);
        return mapToResponse(getActiveRoom(details));
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private OTRoomBillingResponse mapToResponse(OTRoomBilling room) {
        return OTRoomBillingResponse.builder()
                .id(room.getId())
                .otBillingDetailsId(room.getOtBillingDetails().getId())
                .roomNumber(room.getRoomNumber())
                .roomName(room.getRoomName())
                .startTime(room.getStartTime())
                .endTime(room.getEndTime())
                .durationMinutes(room.getDurationMinutes())
                .totalHours(room.getTotalHours())
                .ratePerHour(room.getRatePerHour())
                .baseAmount(room.getBaseAmount())
                .discountPercent(room.getDiscountPercent())
                .discountAmount(room.getDiscountAmount())
                .priceAfterDiscount(room.getPriceAfterDiscount())
                .gstPercent(room.getGstPercent())
                .gstAmount(room.getGstAmount())
                .totalAmount(room.getTotalAmount())
                .isCurrent(room.getEndTime() == null)   // endTime null = current
                .createdAt(room.getCreatedAt())
                .build();
    }
}