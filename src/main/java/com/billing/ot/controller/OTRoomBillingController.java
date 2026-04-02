package com.billing.ot.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.billing.ot.dto.ApiResponse;
import com.billing.ot.dto.OTRoomBillingEndRequest;
import com.billing.ot.dto.OTRoomBillingRequest;
import com.billing.ot.dto.OTRoomBillingResponse;
import com.billing.ot.dto.OTRoomBillingUpdateRequest;
import com.billing.ot.dto.OTRoomShiftRequest;
import com.billing.ot.service.OTRoomBillingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/billing/ot/room")
@RequiredArgsConstructor
public class OTRoomBillingController {

    private final OTRoomBillingService otRoomBillingService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<OTRoomBillingResponse>> createRoomBilling(
            @RequestBody OTRoomBillingRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Room billing created successfully",
                        otRoomBillingService.createRoomBilling(request)));
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse<OTRoomBillingResponse>> updateRoomBilling(
            @RequestBody OTRoomBillingUpdateRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Room billing updated successfully",
                otRoomBillingService.updateRoomBilling(request)));
    }

    @PatchMapping("/end-time")
    public ResponseEntity<ApiResponse<OTRoomBillingResponse>> setEndTime(
            @RequestBody OTRoomBillingEndRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Room end time set successfully",
                otRoomBillingService.setEndTime(request)));
    }

    @PostMapping("/shift")
    public ResponseEntity<ApiResponse<List<OTRoomBillingResponse>>> shiftRoom(
            @RequestBody OTRoomShiftRequest request) {

        return ResponseEntity.ok(ApiResponse.success("Room shifted successfully",
                otRoomBillingService.shiftRoom(request)));
    }

    @GetMapping("/operation/{operationId}")
    public ResponseEntity<ApiResponse<List<OTRoomBillingResponse>>> getRoomBillingByOperationId(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Room billing fetched successfully",
                otRoomBillingService.getRoomBillingByOperationId(operationId)));
    }

    @GetMapping("/operation/{operationId}/current")
    public ResponseEntity<ApiResponse<OTRoomBillingResponse>> getCurrentRoom(
            @PathVariable Long operationId) {

        return ResponseEntity.ok(ApiResponse.success("Current room fetched successfully",
                otRoomBillingService.getCurrentRoom(operationId)));
    }
}