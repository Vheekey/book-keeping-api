package com.calvary.finance.reimbursement;

import com.calvary.finance.audit.AuditLogService;
import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReimbursementService {
    private final ReimbursementRepository reimbursementRepository;
    private final AuditLogService auditLogService;

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

        Reimbursement savedNewReimbursement = reimbursementRepository.save(newReimbursement);
        auditLogService.log(
                "REIMBURSEMENT_CREATED",
                "Reimbursement",
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
}
