package com.calvary.finance.receipt;

import com.calvary.finance.reimbursement.Reimbursement;
import com.calvary.finance.reimbursement.ReimbursementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;

@Service
public class ReceiptService {
    private final ReimbursementRepository reimbursementRepository;
    private final ReimbursementReceiptRepository receiptRepository;
    private final ReceiptStorageService receiptStorageService;
    private final ReceiptProcessingService receiptProcessingService;

    public ReceiptService(
            ReimbursementRepository reimbursementRepository,
            ReimbursementReceiptRepository receiptRepository,
            ReceiptStorageService receiptStorageService,
            ReceiptProcessingService receiptProcessingService
    ) {
        this.reimbursementRepository = reimbursementRepository;
        this.receiptRepository = receiptRepository;
        this.receiptStorageService = receiptStorageService;
        this.receiptProcessingService = receiptProcessingService;
    }

    @Transactional
    public ReceiptResponse uploadReceipt(Long reimbursementId, MultipartFile receiptFile) {
        Reimbursement reimbursement = reimbursementRepository.findById(reimbursementId)
                .orElseThrow(() -> new EntityNotFoundException("Reimbursement not found"));
        StagedReceipt stagedReceipt = receiptStorageService.stage(receiptFile);

        ReimbursementReceipt receipt = new ReimbursementReceipt();
        receipt.setReimbursement(reimbursement);
        receipt.setStatus(ReceiptProcessingStatus.PENDING);
        receipt.setOriginalFilename(stagedReceipt.getOriginalFilename());
        receipt.setSourceContentType(stagedReceipt.getContentType());
        receipt.setOriginalSizeBytes(stagedReceipt.getOriginalSizeBytes());
        receipt.setStagedPath(stagedReceipt.getPath());

        ReimbursementReceipt savedReceipt = receiptRepository.save(receipt);
        runAfterCommit(() -> receiptProcessingService.processAsync(savedReceipt.getId()));

        return new ReceiptResponse()
                .setMessage("Receipt uploaded")
                .setReceipts(List.of(savedReceipt));
    }

    public ReceiptResponse getReceipts(Long reimbursementId) {
        ensureReimbursementExists(reimbursementId);
        return new ReceiptResponse()
                .setMessage("Receipts retrieved")
                .setReceipts(receiptRepository.findAllByReimbursementIdOrderByIdDesc(reimbursementId));
    }

    public ReceiptResponse getReceipt(Long reimbursementId, Long receiptId) {
        return new ReceiptResponse()
                .setMessage("Receipt retrieved")
                .setReceipts(List.of(findReceipt(reimbursementId, receiptId)));
    }

    public ReceiptFile getReceiptFile(Long reimbursementId, Long receiptId) {
        ReimbursementReceipt receipt = findReceipt(reimbursementId, receiptId);
        if (!ReceiptProcessingStatus.COMPLETED.equals(receipt.getStatus())
                || receipt.getStoredPath() == null
                || receipt.getStoredContentType() == null) {
            throw new IllegalArgumentException("Receipt has not completed processing");
        }

        Resource resource = receiptStorageService.processedResource(receipt.getStoredPath());
        return ReceiptFile.builder()
                .resource(resource)
                .filename(filenameForDownload(receipt))
                .contentType(receipt.getStoredContentType())
                .build();
    }

    @Transactional
    public ReceiptResponse retryReceiptProcessing(Long reimbursementId, Long receiptId) {
        ReimbursementReceipt receipt = findReceipt(reimbursementId, receiptId);
        if (ReceiptProcessingStatus.PROCESSING.equals(receipt.getStatus())) {
            throw new IllegalArgumentException("Receipt is already processing");
        }
        if (receipt.getStagedPath() == null) {
            throw new IllegalArgumentException("Receipt cannot be retried because the staged file is missing");
        }

        receipt.setStatus(ReceiptProcessingStatus.PENDING);
        receipt.setErrorMessage(null);
        ReimbursementReceipt savedReceipt = receiptRepository.save(receipt);
        runAfterCommit(() -> receiptProcessingService.processAsync(savedReceipt.getId()));

        return new ReceiptResponse()
                .setMessage("Receipt processing queued")
                .setReceipts(List.of(savedReceipt));
    }

    @Transactional
    public ReceiptResponse deleteReceipt(Long reimbursementId, Long receiptId) {
        ReimbursementReceipt receipt = findReceipt(reimbursementId, receiptId);
        if (ReceiptProcessingStatus.PROCESSING.equals(receipt.getStatus())) {
            throw new IllegalArgumentException("Receipt is processing and cannot be deleted");
        }

        receiptRepository.delete(receipt);
        return new ReceiptResponse()
                .setMessage("Receipt deleted")
                .setReceipts(List.of());
    }

    private void ensureReimbursementExists(Long reimbursementId) {
        if (!reimbursementRepository.existsById(reimbursementId)) {
            throw new EntityNotFoundException("Reimbursement not found");
        }
    }

    private ReimbursementReceipt findReceipt(Long reimbursementId, Long receiptId) {
        return receiptRepository.findByIdAndReimbursementId(receiptId, reimbursementId)
                .orElseThrow(() -> new EntityNotFoundException("Receipt not found"));
    }

    private String filenameForDownload(ReimbursementReceipt receipt) {
        if (receipt.getOriginalFilename() != null && !receipt.getOriginalFilename().isBlank()) {
            return receipt.getOriginalFilename().replace("\"", "");
        }
        String extension = Objects.equals(receipt.getStoredContentType(), "application/pdf") ? ".pdf" : ".jpg";
        return "receipt-" + receipt.getId() + extension;
    }

    private void runAfterCommit(Runnable task) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            task.run();
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                task.run();
            }
        });
    }
}
