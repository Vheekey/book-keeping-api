package com.calvary.finance.reimbursement;


import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/book-keeping/reimbursements")
public class ReimbursementController {

    private final ReimbursementService reimbursementService;

    public ReimbursementController(ReimbursementService reimbursementService) {
        this.reimbursementService = reimbursementService;
    }

    @PostMapping(value = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity<ReimbursementResponse> createReimbursement(
            @Valid @RequestBody CreateReimbursementRequest createReimbursementRequest
    ) {
        ReimbursementResponse response = reimbursementService.createNewReimbursement(createReimbursementRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("{reimbursementId}/approve")
    public ResponseEntity<ReimbursementResponse> approveReimbursement(
            @PathVariable Long reimbursementId, @RequestBody Map<String, String> request
    ) {
        ReimbursementResponse response = reimbursementService.approveNewReimbursement(
                reimbursementId,
                request.get("comment"),
                Boolean.parseBoolean(request.getOrDefault("isApproved", "false"))
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("{reimbursementId}/payout")
    public ResponseEntity<ReimbursementResponse> payoutReimbursement(@PathVariable Long reimbursementId) {
        ReimbursementResponse response = reimbursementService.payoutReimbursement(reimbursementId);
        return ResponseEntity.ok(response);
    }
}
