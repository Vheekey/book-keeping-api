package com.calvary.finance.receipt;

import com.calvary.finance.reimbursement.Reimbursement;
import com.calvary.finance.reimbursement.ReimbursementRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.io.ByteArrayResource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptServiceTest {
    @Mock
    private ReimbursementRepository reimbursementRepository;

    @Mock
    private ReimbursementReceiptRepository receiptRepository;

    @Mock
    private ReceiptStorageService receiptStorageService;

    @Mock
    private ReceiptProcessingService receiptProcessingService;

    @InjectMocks
    private ReceiptService receiptService;

    @Test
    void uploadReceiptStagesFileCreatesReceiptAndQueuesProcessing() {
        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setId(10L);
        MockMultipartFile receiptFile = new MockMultipartFile(
                "receipt",
                "fuel.png",
                "image/png",
                "receipt".getBytes()
        );
        when(reimbursementRepository.findById(10L)).thenReturn(Optional.of(reimbursement));
        when(receiptStorageService.stage(receiptFile)).thenReturn(StagedReceipt.builder()
                .path("2026/04/raw.png")
                .contentType("image/png")
                .originalFilename("fuel.png")
                .originalSizeBytes(100L)
                .build());
        when(receiptRepository.save(any(ReimbursementReceipt.class))).thenAnswer(invocation -> {
            ReimbursementReceipt receipt = invocation.getArgument(0);
            receipt.setId(25L);
            return receipt;
        });

        ReceiptResponse response = receiptService.uploadReceipt(10L, receiptFile);

        ArgumentCaptor<ReimbursementReceipt> receiptCaptor = ArgumentCaptor.forClass(ReimbursementReceipt.class);
        verify(receiptRepository).save(receiptCaptor.capture());
        ReimbursementReceipt savedReceipt = receiptCaptor.getValue();
        assertThat(savedReceipt.getReimbursement()).isEqualTo(reimbursement);
        assertThat(savedReceipt.getStatus()).isEqualTo(ReceiptProcessingStatus.PENDING);
        assertThat(savedReceipt.getOriginalFilename()).isEqualTo("fuel.png");
        assertThat(savedReceipt.getSourceContentType()).isEqualTo("image/png");
        assertThat(savedReceipt.getOriginalSizeBytes()).isEqualTo(100L);
        assertThat(savedReceipt.getStagedPath()).isEqualTo("2026/04/raw.png");
        assertThat(response.getMessage()).isEqualTo("Receipt upload queued");
        assertThat(response.getReceipts()).containsExactly(savedReceipt);
        verify(receiptProcessingService).processAsync(25L);
    }

    @Test
    void uploadReceiptThrowsWhenReimbursementDoesNotExist() {
        MockMultipartFile receiptFile = new MockMultipartFile(
                "receipt",
                "fuel.png",
                "image/png",
                "receipt".getBytes()
        );
        when(reimbursementRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> receiptService.uploadReceipt(404L, receiptFile))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Reimbursement not found");
    }

    @Test
    void getReceiptsReturnsReceiptsForReimbursement() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.COMPLETED);
        when(reimbursementRepository.existsById(10L)).thenReturn(true);
        when(receiptRepository.findAllByReimbursementIdOrderByIdDesc(10L)).thenReturn(List.of(receipt));

        ReceiptResponse response = receiptService.getReceipts(10L);

        assertThat(response.getMessage()).isEqualTo("Receipts retrieved");
        assertThat(response.getReceipts()).containsExactly(receipt);
    }

    @Test
    void getReceiptsThrowsWhenReimbursementDoesNotExist() {
        when(reimbursementRepository.existsById(404L)).thenReturn(false);

        assertThatThrownBy(() -> receiptService.getReceipts(404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Reimbursement not found");
    }

    @Test
    void getReceiptReturnsSpecificReceipt() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.COMPLETED);
        when(receiptRepository.findByIdAndReimbursementId(25L, 10L)).thenReturn(Optional.of(receipt));

        ReceiptResponse response = receiptService.getReceipt(10L, 25L);

        assertThat(response.getMessage()).isEqualTo("Receipt retrieved");
        assertThat(response.getReceipts()).containsExactly(receipt);
    }

    @Test
    void getReceiptFileRequiresCompletedReceipt() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.PENDING);
        when(receiptRepository.findByIdAndReimbursementId(25L, 10L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> receiptService.getReceiptFile(10L, 25L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Receipt has not completed processing");
    }

    @Test
    void getReceiptFileReturnsProcessedResource() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.COMPLETED);
        receipt.setOriginalFilename("fuel.png");
        receipt.setStoredPath("2026/04/hash.jpg");
        receipt.setStoredContentType("image/jpeg");
        ByteArrayResource resource = new ByteArrayResource("receipt".getBytes());
        when(receiptRepository.findByIdAndReimbursementId(25L, 10L)).thenReturn(Optional.of(receipt));
        when(receiptStorageService.processedResource("2026/04/hash.jpg")).thenReturn(resource);

        ReceiptFile receiptFile = receiptService.getReceiptFile(10L, 25L);

        assertThat(receiptFile.getResource()).isEqualTo(resource);
        assertThat(receiptFile.getFilename()).isEqualTo("fuel.png");
        assertThat(receiptFile.getContentType()).isEqualTo("image/jpeg");
    }

    @Test
    void retryReceiptProcessingQueuesReceipt() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.FAILED);
        receipt.setStagedPath("2026/04/raw.png");
        receipt.setErrorMessage("failed");
        when(receiptRepository.findByIdAndReimbursementId(25L, 10L)).thenReturn(Optional.of(receipt));
        when(receiptRepository.save(receipt)).thenReturn(receipt);

        ReceiptResponse response = receiptService.retryReceiptProcessing(10L, 25L);

        assertThat(receipt.getStatus()).isEqualTo(ReceiptProcessingStatus.PENDING);
        assertThat(receipt.getErrorMessage()).isNull();
        assertThat(response.getMessage()).isEqualTo("Receipt processing queued");
        verify(receiptProcessingService).processAsync(25L);
    }

    @Test
    void retryReceiptProcessingRejectsProcessingReceipt() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.PROCESSING);
        when(receiptRepository.findByIdAndReimbursementId(25L, 10L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> receiptService.retryReceiptProcessing(10L, 25L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Receipt is already processing");
    }

    @Test
    void deleteReceiptDeletesNonProcessingReceipt() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.FAILED);
        when(receiptRepository.findByIdAndReimbursementId(25L, 10L)).thenReturn(Optional.of(receipt));

        ReceiptResponse response = receiptService.deleteReceipt(10L, 25L);

        assertThat(response.getMessage()).isEqualTo("Receipt deleted");
        assertThat(response.getReceipts()).isEmpty();
        verify(receiptRepository).delete(receipt);
    }

    @Test
    void deleteReceiptRejectsProcessingReceipt() {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.PROCESSING);
        when(receiptRepository.findByIdAndReimbursementId(25L, 10L)).thenReturn(Optional.of(receipt));

        assertThatThrownBy(() -> receiptService.deleteReceipt(10L, 25L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Receipt is processing and cannot be deleted");
    }

    private ReimbursementReceipt receipt(Long id, ReceiptProcessingStatus status) {
        ReimbursementReceipt receipt = new ReimbursementReceipt();
        receipt.setId(id);
        receipt.setStatus(status);
        return receipt;
    }
}
