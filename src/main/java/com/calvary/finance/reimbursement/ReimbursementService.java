package com.calvary.finance.reimbursement;

import com.calvary.finance.audit.AuditLogService;
import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReimbursementService {
    private final ReimbursementRepository reimbursementRepository;
    private final AuditLogService auditLogService;
    private static final String ENTITY = "Reimbursement";

    public ReimbursementService(
            ReimbursementRepository reimbursementRepository,
            AuditLogService auditLogService
    ) {
        this.reimbursementRepository = reimbursementRepository;
        this.auditLogService = auditLogService;
    }

    @Transactional
    public ReimbursementResponse createNewReimbursement(CreateReimbursementRequest createReimbursementRequest) {
        Reimbursement newReimbursement = new Reimbursement();
        newReimbursement.setAmount(createReimbursementRequest.getAmount());
        newReimbursement.setDescription(createReimbursementRequest.getDescription());
        newReimbursement.setExpenditureDate(createReimbursementRequest.getExpenditureDate());
        newReimbursement.setName(createReimbursementRequest.getName());
        newReimbursement.setShouldReimburse(createReimbursementRequest.getShouldReimburse());
        newReimbursement.setAccountNumber(createReimbursementRequest.getAccountNumber());
        newReimbursement.setAccountName(createReimbursementRequest.getAccountName());
        newReimbursement.setAccNo(createReimbursementRequest.getAccNo());
        newReimbursement.setClearingNumber(createReimbursementRequest.getClearingNumber());
        newReimbursement.setPhoneNumber(createReimbursementRequest.getPhoneNumber());
        newReimbursement.setCorrect(createReimbursementRequest.getIsCorrect());
        newReimbursement.setStatus(ReimbursementStatus.PENDING);

        Reimbursement savedNewReimbursement = reimbursementRepository.save(newReimbursement);
        auditLogService.log(
                "REIMBURSEMENT_CREATED",
                ENTITY,
                String.valueOf(savedNewReimbursement.getId()),
                savedNewReimbursement.getName(),
                buildAuditDetails(savedNewReimbursement)
        );

        ReimbursementResponse response = new ReimbursementResponse();
        response.setMessage("Reimbursement created");
        response.setReimbursements(List.of(savedNewReimbursement));

        return response;
    }

    private Map<String, Object> buildAuditDetails(Reimbursement reimbursement) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("amount", reimbursement.getAmount());
        details.put("description", reimbursement.getDescription());
        details.put("expenditureDate", reimbursement.getExpenditureDate());
        details.put("name", reimbursement.getName());
        details.put("shouldReimburse", reimbursement.isShouldReimburse());
        details.put("accNo", reimbursement.getAccNo());
        details.put("isCorrect", reimbursement.isCorrect());
        return details;
    }

    @Transactional
    public ReimbursementResponse approveNewReimbursement(Long reimbursementId, String comment, Boolean isApproved) {
        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new EntityNotFoundException("Reimbursement not found"));
        Long userId = 1L; //TODO: set authenticated user id

        ReimbursementStatus status = (isApproved == true) ? ReimbursementStatus.APPROVED : ReimbursementStatus.REJECTED;
        reimbursement.setAdminComment(comment);
        reimbursement.setStatus(status);
        reimbursement.setProcessedBy(userId);
        reimbursement.setProcessedAt(Instant.now());
        Reimbursement savedReimbursement = reimbursementRepository.save(reimbursement);
        auditLogService.log("REIMBURSEMENT_APPROVAL_DECISION",
                ENTITY,
                String.valueOf(savedReimbursement.getId()),
                String.valueOf(userId),
                buildAuditDetails(savedReimbursement));

        return new ReimbursementResponse()
                .setMessage("Reimbursement Processed")
                .setReimbursements(List.of(savedReimbursement));
    }

    @Transactional
    public ReimbursementResponse payoutReimbursement(Long reimbursementId) {
        Reimbursement reimbursement = reimbursementRepository
                .findById(reimbursementId).orElseThrow(() -> new EntityNotFoundException("Reimbursement not found"));

        if (!reimbursement.getStatus().equals(ReimbursementStatus.APPROVED)) {
            throw new RuntimeException("Reimbursement not approved for payout");
        }

        Long adminId = 1L; //TODO: Use authenticated user
        reimbursement.setStatus(ReimbursementStatus.PAID);
        reimbursement.setPaidOutBy(adminId);
        reimbursement.setPaidOutAt(Instant.now());
        Reimbursement saved = reimbursementRepository.save(reimbursement);

        auditLogService.log("REIMBURSEMENT_PAID",
                ENTITY,
                String.valueOf(reimbursement.getId()),
                String.valueOf(adminId),
                buildAuditDetails(reimbursement)
        );

        return new ReimbursementResponse().setMessage("Reimbursement paid").setReimbursements(List.of(saved));
    }
}
