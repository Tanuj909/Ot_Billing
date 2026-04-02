//package com.billing.ot.serviceImpl;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//import org.springframework.stereotype.Service;
//
//import com.billing.enums.PaymentStatus;
//import com.billing.exception.ResourceNotFoundException;
//import com.billing.exception.ValidationException;
//import com.billing.model.BillingMaster;
//import com.billing.ot.dto.BillingMasterRequest;
//import com.billing.ot.dto.BillingMasterResponse;
//import com.billing.ot.dto.BillingMasterUpdateRequest;
//import com.billing.ot.service.BillingMasterService;
//import com.billing.repository.BillingMasterRepository;
//
//import jakarta.transaction.Transactional;
//import lombok.RequiredArgsConstructor;
//
//@Service
//@RequiredArgsConstructor
//public class BillingMasterServiceImpl implements BillingMasterService {
//
//    private final BillingMasterRepository billingMasterRepository;
//
//    // ---------------------------------------- Create ---------------------------------------- //
//
//    @Transactional
//    @Override
//    public BillingMasterResponse createBilling(BillingMasterRequest request) {
//
//        // Duplicate check
//        if (request.getOtOperationId() != null &&
//                billingMasterRepository.existsByOtOperationId(request.getOtOperationId())) {
//            throw new ValidationException("Billing already exists for operation: "
//                    + request.getOtOperationId());
//        }
//
//        BillingMaster billing = BillingMaster.builder()
//                .hospitaExternallId(request.getHospitalExternalId())
//                .patientExternalId(request.getPatientExternalId())
//                .otOperationId(request.getOtOperationId())
//                .moduleType(request.getModuleType())
//                .totalAmount(0.0)
//                .paymentStatus(PaymentStatus.PENDING)
//                .paymentMode(request.getPaymentMode())
//                .advancePaymentMode(request.getAdvancePaymentMode())
//                .build();
//
//        billingMasterRepository.save(billing);
//        return mapToResponse(billing);
//    }
//
//    // ----------------------------------------- Get ------------------------------------------ //
//
//    @Override
//    public BillingMasterResponse getBillingById(Long billingId) {
//        return mapToResponse(billingMasterRepository.findById(billingId)
//                .orElseThrow(() -> new ResourceNotFoundException("Billing not found")));
//    }
//
//    @Override
//    public BillingMasterResponse getBillingByOperationId(Long operationId) {
//        return mapToResponse(billingMasterRepository.findByOtOperationId(operationId)
//                .orElseThrow(() -> new ResourceNotFoundException(
//                        "Billing not found for operation: " + operationId)));
//    }
//
//    @Override
//    public List<BillingMasterResponse> getBillingByPatientId(Long patientId) {
//        return billingMasterRepository.findByPatientExternalId(patientId)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    @Override
//    public List<BillingMasterResponse> getBillingByModuleType(String moduleType) {
//        return billingMasterRepository.findByModuleType(moduleType)
//                .stream()
//                .map(this::mapToResponse)
//                .collect(Collectors.toList());
//    }
//
//    // ---------------------------------------- Update ---------------------------------------- //
//
//    @Transactional
//    @Override
//    public BillingMasterResponse updateBilling(Long billingId, BillingMasterUpdateRequest request) {
//
//        BillingMaster billing = billingMasterRepository.findById(billingId)
//                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));
//
//        if (billing.getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
//            throw new ValidationException("Cancelled billing cannot be updated");
//        }
//
//        if (request.getTotalAmount() != null)   billing.setTotalAmount(request.getTotalAmount());
//        if (request.getPaymentStatus() != null) billing.setPaymentStatus(request.getPaymentStatus());
//        if (request.getPaymentMode() != null)   billing.setPaymentMode(request.getPaymentMode());
//
//        billingMasterRepository.save(billing);
//        return mapToResponse(billing);
//    }
//
//    // ---------------------------------------- Cancel ---------------------------------------- //
//
//    @Transactional
//    @Override
//    public void cancelBilling(Long billingId) {
//
//        BillingMaster billing = billingMasterRepository.findById(billingId)
//                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));
//
//        if (billing.getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
//            throw new ValidationException("Billing is already cancelled");
//        }
//
//        if (billing.getPaymentStatus().equals(PaymentStatus.PAID)) {
//            throw new ValidationException("Paid billing cannot be cancelled — initiate refund instead");
//        }
//
//        billing.setPaymentStatus(PaymentStatus.CANCELLED);
//        billingMasterRepository.save(billing);
//    }
//
//    // ---------------------------------------- Mapper ---------------------------------------- //
//
//    private BillingMasterResponse mapToResponse(BillingMaster billing) {
//        return BillingMasterResponse.builder()
//                .id(billing.getId())
//                .hospitalExternalId(billing.getHospitaExternallId())
//                .patientExternalId(billing.getPatientExternalId())
//                .otOperationId(billing.getOtOperationId())
//                .moduleType(billing.getModuleType())
//                .totalAmount(billing.getTotalAmount())
//                .paymentStatus(billing.getPaymentStatus())
//                .paymentMode(billing.getPaymentMode())
//                .advancePaymentMode(billing.getAdvancePaymentMode())
//                .billingDate(billing.getBillingDate())
//                .updatedAt(billing.getUpdatedAt())
//                .build();
//    }
//}

package com.billing.ot.serviceImpl;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import com.billing.enums.PaymentStatus;
import com.billing.exception.ResourceNotFoundException;
import com.billing.exception.ValidationException;
import com.billing.model.BillingMaster;
import com.billing.ot.dto.BillingMasterResponse;
import com.billing.ot.dto.BillingMasterUpdateRequest;
import com.billing.ot.dto.OTBillingMasterRequest;
import com.billing.ot.service.BillingMasterService;
import com.billing.repository.BillingMasterRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BillingMasterServiceImpl implements BillingMasterService {

    private final BillingMasterRepository billingMasterRepository;

    /**
     * Terminal statuses — a billing in one of these states is considered "done",
     * so we allow a fresh billing for the same patient + module + hospital.
     */
    private static final List<PaymentStatus> TERMINAL_STATUSES =
            List.of(PaymentStatus.CANCELLED, PaymentStatus.PAID);

    // ---------------------------------------- Create ---------------------------------------- //

    @Transactional
    @Override
    public BillingMasterResponse createBilling(OTBillingMasterRequest request) {

        // ── Validation 1: operationId must be present (already enforced by @NotNull in DTO,
        //                  but double-checked here as a safety net) ──────────────────────────
        if (request.getOtOperationId() == null) {
            throw new ValidationException("Operation ID is required");
        }

        // ── Validation 2: Duplicate operationId check ─────────────────────────────────────
        // Each OT operation can have exactly ONE billing entry ever — even if previous
        // was cancelled.  An operation is a unique event; re-billing the same operation
        // would be a data error.
        if (billingMasterRepository.existsByOtOperationId(request.getOtOperationId())) {
            throw new ValidationException(
                    "Billing already exists for operation ID: " + request.getOtOperationId());
        }

        // ── Validation 3: Active billing check for same patient + moduleType + hospital ────
        // Scenario: Patient already has a PENDING / PARTIALLY_PAID OT billing →
        //           block new entry until that billing is closed (PAID) or cancelled.
        // If previous billing is PAID or CANCELLED → allow new entry freely.
        boolean activeEntryExists =
                billingMasterRepository
                        .existsByPatientExternalIdAndModuleTypeAndHospitaExternallIdAndPaymentStatusNotIn(
                                request.getPatientExternalId(),
                                request.getModuleType(),
                                request.getHospitalExternalId(),
                                TERMINAL_STATUSES);

        if (activeEntryExists) {
            throw new ValidationException(
                    "An active billing already exists for patient ID " +
                    request.getPatientExternalId() +
                    " under module '" + request.getModuleType() + "'. " +
                    "Please close or cancel the existing billing before creating a new one.");
        }

        // ── Build and persist ─────────────────────────────────────────────────────────────
        BillingMaster billing = BillingMaster.builder()
                .hospitaExternallId(request.getHospitalExternalId())
                .patientExternalId(request.getPatientExternalId())
                .otOperationId(request.getOtOperationId())
                .moduleType(request.getModuleType())
                .totalAmount(0.0)
                .paymentStatus(PaymentStatus.PENDING)
                .paymentMode(request.getPaymentMode())
                .advancePaymentMode(request.getAdvancePaymentMode())
                .build();

        billingMasterRepository.save(billing);
        return mapToResponse(billing);
    }

    // ----------------------------------------- Get ------------------------------------------ //

    @Override
    public BillingMasterResponse getBillingById(Long billingId) {
        return mapToResponse(billingMasterRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found")));
    }

    @Override
    public BillingMasterResponse getBillingByOperationId(Long operationId) {
        return mapToResponse(billingMasterRepository.findByOtOperationId(operationId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Billing not found for operation: " + operationId)));
    }

    @Override
    public List<BillingMasterResponse> getBillingByPatientId(Long patientId) {
        return billingMasterRepository.findByPatientExternalId(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BillingMasterResponse> getBillingByModuleType(String moduleType) {
        return billingMasterRepository.findByModuleType(moduleType)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------- Update ---------------------------------------- //

    @Transactional
    @Override
    public BillingMasterResponse updateBilling(Long billingId, BillingMasterUpdateRequest request) {

        BillingMaster billing = billingMasterRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));

        // Guard: terminal billings are immutable
        if (TERMINAL_STATUSES.contains(billing.getPaymentStatus())) {
            throw new ValidationException(
                    "Billing with status '" + billing.getPaymentStatus() + "' cannot be updated");
        }

        if (request.getTotalAmount() != null)   billing.setTotalAmount(request.getTotalAmount());
        if (request.getPaymentStatus() != null) billing.setPaymentStatus(request.getPaymentStatus());
        if (request.getPaymentMode() != null)   billing.setPaymentMode(request.getPaymentMode());

        billingMasterRepository.save(billing);
        return mapToResponse(billing);
    }

    // ---------------------------------------- Cancel ---------------------------------------- //

    @Transactional
    @Override
    public void cancelBilling(Long billingId) {

        BillingMaster billing = billingMasterRepository.findById(billingId)
                .orElseThrow(() -> new ResourceNotFoundException("Billing not found"));

        if (billing.getPaymentStatus().equals(PaymentStatus.CANCELLED)) {
            throw new ValidationException("Billing is already cancelled");
        }

        if (billing.getPaymentStatus().equals(PaymentStatus.PAID)) {
            throw new ValidationException(
                    "Paid billing cannot be cancelled — initiate a refund instead");
        }

        billing.setPaymentStatus(PaymentStatus.CANCELLED);
        billingMasterRepository.save(billing);
    }

    // ---------------------------------------- Mapper ---------------------------------------- //

    private BillingMasterResponse mapToResponse(BillingMaster billing) {
        return BillingMasterResponse.builder()
                .id(billing.getId())
                .hospitalExternalId(billing.getHospitaExternallId())
                .patientExternalId(billing.getPatientExternalId())
                .otOperationId(billing.getOtOperationId())
                .moduleType(billing.getModuleType())
                .totalAmount(billing.getTotalAmount())
                .paymentStatus(billing.getPaymentStatus())
                .paymentMode(billing.getPaymentMode())
                .advancePaymentMode(billing.getAdvancePaymentMode())
                .billingDate(billing.getBillingDate())
                .updatedAt(billing.getUpdatedAt())
                .build();
    }
}