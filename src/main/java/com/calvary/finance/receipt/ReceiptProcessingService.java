package com.calvary.finance.receipt;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ReceiptProcessingService {
    private final ReimbursementReceiptRepository receiptRepository;
    private final ReceiptStorageService receiptStorageService;

    public ReceiptProcessingService(
            ReimbursementReceiptRepository receiptRepository,
            ReceiptStorageService receiptStorageService
    ) {
        this.receiptRepository = receiptRepository;
        this.receiptStorageService = receiptStorageService;
    }

    @Async
    @Transactional
    public void processAsync(Long receiptId) {
        process(receiptId);
    }

    public void process(Long receiptId) {
        ReimbursementReceipt receipt = receiptRepository.findById(receiptId)
                .orElseThrow(() -> new IllegalArgumentException("Receipt not found"));

        receipt.setStatus(ReceiptProcessingStatus.PROCESSING);
        receipt.setErrorMessage(null);
        receiptRepository.save(receipt);

        try {
            StoredReceipt storedReceipt = receiptStorageService.process(
                    receipt.getStagedPath(),
                    receipt.getSourceContentType()
            );
            receipt.setStoredPath(storedReceipt.getPath());
            receipt.setStoredContentType(storedReceipt.getContentType());
            receipt.setStoredSizeBytes(storedReceipt.getStoredSizeBytes());
            receipt.setSha256(storedReceipt.getSha256());
            receipt.setProcessedAt(Instant.now());
            receipt.setStatus(ReceiptProcessingStatus.COMPLETED);
        } catch (RuntimeException ex) {
            receipt.setStatus(ReceiptProcessingStatus.FAILED);
            receipt.setErrorMessage(ex.getMessage());
        }

        receiptRepository.save(receipt);
    }
}
