package com.calvary.finance.receipt;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceiptProcessingServiceTest {
    @Mock
    private ReimbursementReceiptRepository receiptRepository;

    @Mock
    private ReceiptStorageService receiptStorageService;

    @InjectMocks
    private ReceiptProcessingService receiptProcessingService;

    @Test
    void processStoresFinalReceiptMetadataAndMarksCompleted() {
        ReimbursementReceipt receipt = new ReimbursementReceipt();
        receipt.setId(25L);
        receipt.setStagedPath("2026/04/raw.png");
        receipt.setSourceContentType("image/png");
        when(receiptRepository.findById(25L)).thenReturn(Optional.of(receipt));
        when(receiptStorageService.process("2026/04/raw.png", "image/png"))
                .thenReturn(StoredReceipt.builder()
                        .path("2026/04/hash.jpg")
                        .contentType("image/jpeg")
                        .storedSizeBytes(300L)
                        .sha256("hash")
                        .build());

        receiptProcessingService.process(25L);

        assertThat(receipt.getStatus()).isEqualTo(ReceiptProcessingStatus.COMPLETED);
        assertThat(receipt.getStoredPath()).isEqualTo("2026/04/hash.jpg");
        assertThat(receipt.getStoredContentType()).isEqualTo("image/jpeg");
        assertThat(receipt.getStoredSizeBytes()).isEqualTo(300L);
        assertThat(receipt.getSha256()).isEqualTo("hash");
        assertThat(receipt.getProcessedAt()).isNotNull();
        verify(receiptRepository, times(2)).save(receipt);
    }

    @Test
    void processMarksFailedWhenStorageFails() {
        ReimbursementReceipt receipt = new ReimbursementReceipt();
        receipt.setId(25L);
        receipt.setStagedPath("2026/04/raw.txt");
        receipt.setSourceContentType("text/plain");
        when(receiptRepository.findById(25L)).thenReturn(Optional.of(receipt));
        when(receiptStorageService.process("2026/04/raw.txt", "text/plain"))
                .thenThrow(new IllegalArgumentException("Receipt must be a JPEG, PNG, or PDF file"));

        receiptProcessingService.process(25L);

        assertThat(receipt.getStatus()).isEqualTo(ReceiptProcessingStatus.FAILED);
        assertThat(receipt.getErrorMessage()).isEqualTo("Receipt must be a JPEG, PNG, or PDF file");
        verify(receiptRepository, times(2)).save(receipt);
    }
}
