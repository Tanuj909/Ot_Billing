package com.billing.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.billing.dto.AddDoctorVisitsRequest;
import com.billing.dto.AddMedicationsRequest;
import com.billing.dto.AddServicesRequest;
import com.billing.model.IPDDoctorVisit;
import com.billing.model.IPDMedication;
import com.billing.model.IPDServiceUsage;
import com.billing.service.IPDBillingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ipd/billing")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")

public class IPDBillingDetailsController {

    private final IPDBillingService ipdBillingService;

    @PostMapping("/{ipdBillingId}/services")
    public ResponseEntity<List<IPDServiceUsage>> addServices(
            @PathVariable Long ipdBillingId,
            @RequestBody AddServicesRequest request) {
        request.setIpdBillingId(ipdBillingId);
        return ResponseEntity.ok(ipdBillingService.addServices(request));
    }

    @PostMapping("/{ipdBillingId}/doctor-visits")
    public ResponseEntity<List<IPDDoctorVisit>> addDoctorVisits(
            @PathVariable Long ipdBillingId,
            @RequestBody AddDoctorVisitsRequest request) {
        request.setIpdBillingId(ipdBillingId);
        return ResponseEntity.ok(ipdBillingService.addDoctorVisits(request));
    }

    @PostMapping("/{ipdBillingId}/medications")
    public ResponseEntity<List<IPDMedication>> addMedications(
            @PathVariable Long ipdBillingId,
            @RequestBody AddMedicationsRequest request) {
        request.setIpdBillingId(ipdBillingId);
        return ResponseEntity.ok(ipdBillingService.addMedications(request));
    }

    @GetMapping("/{ipdBillingId}/services")
    public ResponseEntity<List<IPDServiceUsage>> getServices(@PathVariable Long ipdBillingId) {
        return ResponseEntity.ok(ipdBillingService.getServicesByBillingId(ipdBillingId));
    }

    @GetMapping("/{ipdBillingId}/doctor-visits")
    public ResponseEntity<List<IPDDoctorVisit>> getDoctorVisits(@PathVariable Long ipdBillingId) {
        return ResponseEntity.ok(ipdBillingService.getDoctorVisitsByBillingId(ipdBillingId));
    }

    @GetMapping("/{ipdBillingId}/medications")
    public ResponseEntity<List<IPDMedication>> getMedications(@PathVariable Long ipdBillingId) {
        return ResponseEntity.ok(ipdBillingService.getMedicationsByBillingId(ipdBillingId));
    }
}