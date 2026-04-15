package com.billing.ot.mapper;

import org.springframework.stereotype.Component;

import com.billing.ot.dto.OTItemBillingResponse;
import com.billing.ot.dto.OTRecoveryRoomBillingResponse;
import com.billing.ot.dto.OTRoomBillingResponse;
import com.billing.ot.dto.OTStaffBillingResponse;
import com.billing.ot.entity.OTItemBilling;
import com.billing.ot.entity.OTRecoveryRoomBilling;
import com.billing.ot.entity.OTRoomBilling;
import com.billing.ot.entity.OTStaffBilling;

//package com.billing.ot.mapper

@Component
public class OTBillingMapper {

 // ==================== Staff Mapper ==================== //

 public OTStaffBillingResponse mapStaff(OTStaffBilling staffBilling) {
     return OTStaffBillingResponse.builder()
             .id(staffBilling.getId())
             .otBillingDetailsId(staffBilling.getOtBillingDetails().getId())
             .staffExternalId(staffBilling.getStaffExternalId())
             .staffName(staffBilling.getStaffName())
             .staffRole(staffBilling.getStaffRole())
             .fees(staffBilling.getFees())
             .discountPercent(staffBilling.getDiscountPercent())
             .discountAmount(staffBilling.getDiscountAmount())
             .priceAfterDiscount(staffBilling.getPriceAfterDiscount())
             .gstPercent(staffBilling.getGstPercent())
             .gstAmount(staffBilling.getGstAmount())
             .totalAmount(staffBilling.getTotalAmount())
             .serviceAddedAt(staffBilling.getServiceAddedAt())
             .createdAt(staffBilling.getCreatedAt())
             .build();
 }

 // ==================== Room Mapper ==================== //

 public OTRoomBillingResponse mapRoom(OTRoomBilling room) {
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
             .isCurrent(room.getEndTime() == null)
             .createdAt(room.getCreatedAt())
             .build();
 }
 
//==================== Recovery Room Mapper ==================== //

public OTRecoveryRoomBillingResponse mapRecovery(OTRecoveryRoomBilling recovery) {
  if (recovery == null) return null;

  return OTRecoveryRoomBillingResponse.builder()
          .id(recovery.getId())
          .otBillingDetailsId(recovery.getOtBillingDetails().getId())
          .operationExternalId(recovery.getOtBillingDetails().getOperationExternalId())
          .wardRoomId(recovery.getWardRoomId())
          .wardRoomBedId(recovery.getWardRoomBedId())
          .wardRoomName(recovery.getWardRoomName())
          .startTime(recovery.getStartTime())
          .endTime(recovery.getEndTime())
          .durationMinutes(recovery.getDurationMinutes())
          .totalHours(recovery.getTotalHours())
          .ratePerHour(recovery.getRatePerHour())
          .baseAmount(recovery.getBaseAmount())
          .discountPercent(recovery.getDiscountPercent())
          .discountAmount(recovery.getDiscountAmount())
          .priceAfterDiscount(recovery.getPriceAfterDiscount())
          .gstPercent(recovery.getGstPercent())
          .gstAmount(recovery.getGstAmount())
          .totalAmount(recovery.getTotalAmount())
          .isCurrent(recovery.getEndTime() == null)
          .createdAt(recovery.getCreatedAt())
          .updatedAt(recovery.getUpdatedAt())
          .build();
}

 // ==================== Item Mapper ==================== //

 public OTItemBillingResponse mapItem(OTItemBilling item) {
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
