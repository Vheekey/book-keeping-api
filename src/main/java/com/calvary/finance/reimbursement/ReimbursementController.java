package com.calvary.finance.reimbursement;


import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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

    @GetMapping()
    public ResponseEntity<ReimbursementResponse> getReimbursements(
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        ReimbursementResponse response = reimbursementService.getReimbursements(
                pageNumber,
                pageSize,
                status,
                startDate,
                endDate
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("{reimbursementId}")
    public ResponseEntity<ReimbursementResponse> getReimbursement(@PathVariable Long reimbursementId) {
        ReimbursementResponse response = reimbursementService.getReimbursement(reimbursementId);
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
