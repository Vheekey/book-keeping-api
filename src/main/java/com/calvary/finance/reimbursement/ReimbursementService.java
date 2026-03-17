package com.calvary.finance.reimbursement;

import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReimbursementService {
    private final ReimbursementRepository reimbursementRepository;

    public ReimbursementService(ReimbursementRepository reimbursementRepository) {
        this.reimbursementRepository = reimbursementRepository;
    }

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

        ReimbursementResponse response = new ReimbursementResponse();
        response.setMessage("Reimbursement created");
        response.setReimbursements(List.of(savedNewReimbursement));

        //TODO: Add audit log
        return response;
    }
}
