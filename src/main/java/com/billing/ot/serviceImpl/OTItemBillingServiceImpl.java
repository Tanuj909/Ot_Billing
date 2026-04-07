package com.billing.ot.serviceImpl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.billing.enums.PaymentStatus;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ValidationException;
import com.billing.ot.dto.OTItemBillingRequest;
import com.billing.ot.dto.OTItemBillingResponse;
import com.billing.ot.dto.OTItemBillingSummaryResponse;
import com.billing.ot.dto.OTItemBillingUpdateRequest;
import com.billing.ot.entity.OTBillingDetails;
import com.billing.ot.entity.OTItemBilling;
import com.billing.ot.enums.OTItemType;
import com.billing.ot.repository.OTBillingDetailsRepository;
import com.billing.ot.repository.OTItemBillingRepository;
import com.billing.ot.service.OTBillingDetailsService;
import com.billing.ot.service.OTItemBillingService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OTItemBillingServiceImpl implements OTItemBillingService {

    private final OTItemBillingRepository itemBillingRepository;
    private final OTBillingDetailsRepository otBillingDetailsRepository;
    private final OTBillingDetailsService otBillingDetailsService;

    // Helper
    private OTBillingDetails getDetails(Long operationId) {
        return otBillingDetailsRepository
                .findByOperationExternalId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "OT Billing not found for operation: " + operationId));
    }

    private void validateBillingActive(OTBillingDetails details) {
        if (!details.getBillingStatus().equals("ACTIVE")) {
            throw new ValidationException("Cannot proceed — billing is "
                    + details.getBillingStatus());
        }
        if (details.getBillingMaster().getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Cannot proceed — billing master is CANCELLED");
        }
    }

    // ---------------------------------------- Add ---------------------------------------- //

    @Transactional
    @Override
    public OTItemBillingResponse addItem(OTItemBillingRequest request) {

        OTBillingDetails details = getDetails(request.getOperationExternalId());
        validateBillingActive(details);

        // Duplicate check
//        if (itemBillingRepository.existsByOtBillingDetailsAndItemExternalId(
//                details, request.getItemExternalId())) {
//            throw new ValidationException("Item already added to billing: "
//                    + request.getItemName());
//        }


        Integer quantity = (request.getQuantity() == null || request.getQuantity() <= 0)
                ? 1
                : request.getQuantity();
        
        
        OTItemBilling item = OTItemBilling.builder()
                .otBillingDetails(details)
                .itemExternalId(request.getItemExternalId())
                .itemType(request.getItemType())
                .itemName(request.getItemName())
                .itemCode(request.getItemCode())
                .hsnCode(request.getHsnCode())
                .quantity(quantity)
                .unitPrice(request.getUnitPrice())
                .discountPercent(request.getDiscountPercent())
                .gstPercent(request.getGstPercent())
                .build();

        itemBillingRepository.save(item);
        otBillingDetailsService.recalculateTotals(request.getOperationExternalId());

        return mapToResponse(item);
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public OTItemBillingResponse updateItem(Long itemBillingId,
            OTItemBillingUpdateRequest request) {

        OTItemBilling item = itemBillingRepository.findById(itemBillingId)
                .orElseThrow(() -> new ResourceNotFoundException("Item billing not found"));

        validateBillingActive(item.getOtBillingDetails());

        if (request.getQuantity() != null)        item.setQuantity(request.getQuantity());
        if (request.getUnitPrice() != null)       item.setUnitPrice(request.getUnitPrice());
        if (request.getDiscountPercent() != null) item.setDiscountPercent(request.getDiscountPercent());
        if (request.getGstPercent() != null)      item.setGstPercent(request.getGstPercent());

        item.calculateAmounts();
        itemBillingRepository.save(item);

        otBillingDetailsService.recalculateTotals(
                item.getOtBillingDetails().getOperationExternalId());

        return mapToResponse(item);
    }

    // ---------------------------------------- Remove ---------------------------------------- //

    @Transactional
    @Override
    public void removeItem(Long itemBillingId) {

        OTItemBilling item = itemBillingRepository.findById(itemBillingId)
                .orElseThrow(() -> new ResourceNotFoundException("Item billing not found"));

        OTBillingDetails billing = item.getOtBillingDetails();

        validateBillingActive(billing);

        Long operationId = billing.getOperationExternalId();

        // ✅ Correct deletion
        billing.getItemCharges().remove(item);

        // optional but safe
        itemBillingRepository.flush();

        // Recalculate
        otBillingDetailsService.recalculateTotals(operationId);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public List<OTItemBillingResponse> getItemsByOperationId(Long operationId) {
        OTBillingDetails details = getDetails(operationId);
        return itemBillingRepository.findByOtBillingDetails(details)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<OTItemBillingResponse> getItemsByType(Long operationId, OTItemType itemType) {
        OTBillingDetails details = getDetails(operationId);
        return itemBillingRepository.findByOtBillingDetailsAndItemType(details, itemType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public OTItemBillingSummaryResponse getItemSummary(Long operationId) {

        OTBillingDetails details = getDetails(operationId);

        List<OTItemBillingResponse> allItems = itemBillingRepository
                .findByOtBillingDetails(details)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        // Group by itemType
        Map<OTItemType, List<OTItemBillingResponse>> byItemType = allItems.stream()
                .collect(Collectors.groupingBy(OTItemBillingResponse::getItemType));

        // Total by itemType
        Map<OTItemType, Double> totalByItemType = allItems.stream()
                .collect(Collectors.groupingBy(
                        OTItemBillingResponse::getItemType,
                        Collectors.summingDouble(i -> i.getTotalAmount() != null
                                ? i.getTotalAmount() : 0.0)));

        // Grand total
        Double grandTotal = allItems.stream()
                .mapToDouble(i -> i.getTotalAmount() != null ? i.getTotalAmount() : 0.0)
                .sum();

        return OTItemBillingSummaryResponse.builder()
                .operationExternalId(operationId)
                .allItems(allItems)
                .byItemType(byItemType)
                .totalByItemType(totalByItemType)
                .grandTotal(grandTotal)
                .build();
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private OTItemBillingResponse mapToResponse(OTItemBilling item) {
        return OTItemBillingResponse.builder()
                .id(item.getId())
                .otBillingDetailsId(item.getOtBillingDetails().getId())
                .itemExternalId(item.getItemExternalId())
                .itemType(item.getItemType())
                .itemName(item.getItemName())
                .itemCode(item.getItemCode())
                .hsnCode(item.getHsnCode())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .discountPercent(item.getDiscountPercent())
                .discountAmount(item.getDiscountAmount())
                .priceAfterDiscount(item.getPriceAfterDiscount())
                .gstPercent(item.getGstPercent())
                .gstAmount(item.getGstAmount())
                .totalAmount(item.getTotalAmount())
                .serviceAddedAt(item.getServiceAddedAt())
                .createdAt(item.getCreatedAt())
                .build();
    }
}