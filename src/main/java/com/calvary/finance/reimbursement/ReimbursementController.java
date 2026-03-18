package com.calvary.finance.reimbursement;


import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/book-keeping/reimbursement")
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
}
