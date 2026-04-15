package com.billing.ot.controller;

import com.billing.ot.dto.*;
import com.billing.ot.service.OTRecoveryRoomBillingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ot/recovery-room")
@RequiredArgsConstructor
public class OTRecoveryRoomBillingController {

    private final OTRecoveryRoomBillingService recoveryRoomService;

    // ==================== CREATE Recovery Room ====================
    @PostMapping
    public ResponseEntity<OTRecoveryRoomBillingResponse> createRecoveryRoom(
            @RequestBody OTRecoveryRoomBillingRequest request) {
        
        OTRecoveryRoomBillingResponse response = recoveryRoomService.createRecoveryRoom(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== UPDATE Recovery Room ====================
    @PutMapping("/{recoveryId}")
    public ResponseEntity<OTRecoveryRoomBillingResponse> updateRecoveryRoom(
            @PathVariable Long recoveryId,
            @RequestBody OTRecoveryRoomBillingUpdateRequest request) {
        
        OTRecoveryRoomBillingResponse response = recoveryRoomService.updateRecoveryRoom(recoveryId, request);
        return ResponseEntity.ok(response);
    }

    // ==================== SET END TIME ====================
    @PutMapping("/end-time")
    public ResponseEntity<OTRecoveryRoomBillingResponse> setEndTime(
            @RequestBody OTRecoveryRoomBillingEndRequest request) {
        
        OTRecoveryRoomBillingResponse response = recoveryRoomService.setEndTime(request);
        return ResponseEntity.ok(response);
    }

    // ==================== DELETE / REMOVE ====================
    @DeleteMapping("/{recoveryId}")
    public ResponseEntity<Void> removeRecoveryRoom(@PathVariable Long recoveryId) {
        recoveryRoomService.removeRecoveryRoom(recoveryId);
        return ResponseEntity.noContent().build();
    }

    // ==================== GET BY ID ====================
    @GetMapping("/{recoveryId}")
    public ResponseEntity<OTRecoveryRoomBillingResponse> getById(@PathVariable Long recoveryId) {
        OTRecoveryRoomBillingResponse response = recoveryRoomService.getById(recoveryId);
        return ResponseEntity.ok(response);
    }

    // ==================== GET BY OPERATION ID ====================
    @GetMapping("/operation/{operationId}")
    public ResponseEntity<List<OTRecoveryRoomBillingResponse>> getByOperationId(
            @PathVariable Long operationId) {
        
        List<OTRecoveryRoomBillingResponse> response = recoveryRoomService.getByOperationId(operationId);
        return ResponseEntity.ok(response);
    }

    // ==================== GET CURRENT RECOVERY ROOM ====================
    @GetMapping("/current/{operationId}")
    public ResponseEntity<OTRecoveryRoomBillingResponse> getCurrentRecoveryRoom(
            @PathVariable Long operationId) {
        
        OTRecoveryRoomBillingResponse response = recoveryRoomService.getCurrentRecoveryRoom(operationId);
        return ResponseEntity.ok(response);
    }
}