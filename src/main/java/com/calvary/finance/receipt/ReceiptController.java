package com.calvary.finance.receipt;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/reimbursements/{reimbursementId}/receipts")
public class ReceiptController {
    private final ReceiptService receiptService;

    public ReceiptController(ReceiptService receiptService) {
        this.receiptService = receiptService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = "application/json")
    public ResponseEntity<ReceiptResponse> uploadReceipt(
            @PathVariable Long reimbursementId,
            @RequestPart MultipartFile receipt
    ) {
        ReceiptResponse response = receiptService.uploadReceipt(reimbursementId, receipt);
        return ResponseEntity.accepted().body(response);
    }

    @PreAuthorize("hasRole('FINANCE')")
    @GetMapping(produces = "application/json")
    public ResponseEntity<ReceiptResponse> getReceipts(@PathVariable Long reimbursementId) {
        ReceiptResponse response = receiptService.getReceipts(reimbursementId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('FINANCE')")
    @GetMapping(value = "{receiptId}", produces = "application/json")
    public ResponseEntity<ReceiptResponse> getReceipt(
            @PathVariable Long reimbursementId,
            @PathVariable Long receiptId
    ) {
        ReceiptResponse response = receiptService.getReceipt(reimbursementId, receiptId);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('FINANCE')")
    @GetMapping("{receiptId}/file")
    public ResponseEntity<Resource> getReceiptFile(
            @PathVariable Long reimbursementId,
            @PathVariable Long receiptId
    ) {
        ReceiptFile receiptFile = receiptService.getReceiptFile(reimbursementId, receiptId);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(receiptFile.getContentType()))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + receiptFile.getFilename() + "\""
                )
                .body(receiptFile.getResource());
    }

    @PreAuthorize("hasRole('FINANCE')")
    @PostMapping(value = "{receiptId}/retry", produces = "application/json")
    public ResponseEntity<ReceiptResponse> retryReceiptProcessing(
            @PathVariable Long reimbursementId,
            @PathVariable Long receiptId
    ) {
        ReceiptResponse response = receiptService.retryReceiptProcessing(reimbursementId, receiptId);
        return ResponseEntity.accepted().body(response);
    }

    @PreAuthorize("hasRole('FINANCE')")
    @DeleteMapping(value = "{receiptId}", produces = "application/json")
    public ResponseEntity<ReceiptResponse> deleteReceipt(
            @PathVariable Long reimbursementId,
            @PathVariable Long receiptId
    ) {
        ReceiptResponse response = receiptService.deleteReceipt(reimbursementId, receiptId);
        return ResponseEntity.ok(response);
    }
}
