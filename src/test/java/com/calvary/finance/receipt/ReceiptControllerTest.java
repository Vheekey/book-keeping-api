package com.calvary.finance.receipt;

import com.calvary.finance.shared.JwtService;
import com.calvary.finance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReceiptController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReceiptControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReceiptService receiptService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @Test
    void uploadReceiptReturnsAcceptedQueuedReceipt() throws Exception {
        ReimbursementReceipt receipt = new ReimbursementReceipt();
        receipt.setId(25L);
        receipt.setStatus(ReceiptProcessingStatus.PENDING);
        when(receiptService.uploadReceipt(eq(10L), any(MultipartFile.class)))
                .thenReturn(new ReceiptResponse()
                        .setMessage("Receipt upload queued")
                        .setReceipts(List.of(receipt)));

        MockMultipartFile receiptFile = new MockMultipartFile(
                "receipt",
                "fuel.png",
                MediaType.IMAGE_PNG_VALUE,
                "receipt".getBytes()
        );

        mockMvc.perform(multipart("/reimbursements/{reimbursementId}/receipts", 10L)
                        .file(receiptFile))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Receipt upload queued"))
                .andExpect(jsonPath("$.receipts[0].id").value(25))
                .andExpect(jsonPath("$.receipts[0].status").value("PENDING"));

        verify(receiptService).uploadReceipt(eq(10L), any(MultipartFile.class));
    }

    @Test
    void getReceiptsReturnsReceiptList() throws Exception {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.COMPLETED);
        when(receiptService.getReceipts(10L))
                .thenReturn(new ReceiptResponse()
                        .setMessage("Receipts retrieved")
                        .setReceipts(List.of(receipt)));

        mockMvc.perform(get("/reimbursements/{reimbursementId}/receipts", 10L)
                        .principal(financeAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Receipts retrieved"))
                .andExpect(jsonPath("$.receipts[0].id").value(25))
                .andExpect(jsonPath("$.receipts[0].status").value("COMPLETED"));

        verify(receiptService).getReceipts(10L);
    }

    @Test
    void getReceiptReturnsSingleReceipt() throws Exception {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.COMPLETED);
        when(receiptService.getReceipt(10L, 25L))
                .thenReturn(new ReceiptResponse()
                        .setMessage("Receipt retrieved")
                        .setReceipts(List.of(receipt)));

        mockMvc.perform(get("/reimbursements/{reimbursementId}/receipts/{receiptId}", 10L, 25L)
                        .principal(financeAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Receipt retrieved"))
                .andExpect(jsonPath("$.receipts[0].id").value(25));

        verify(receiptService).getReceipt(10L, 25L);
    }

    @Test
    void getReceiptFileReturnsInlineFile() throws Exception {
        when(receiptService.getReceiptFile(10L, 25L))
                .thenReturn(ReceiptFile.builder()
                        .resource(new ByteArrayResource("receipt".getBytes()))
                        .filename("fuel.jpg")
                        .contentType(MediaType.IMAGE_JPEG_VALUE)
                        .build());

        mockMvc.perform(get("/reimbursements/{reimbursementId}/receipts/{receiptId}/file", 10L, 25L)
                        .principal(financeAuthentication()))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Type", MediaType.IMAGE_JPEG_VALUE))
                .andExpect(header().string("Content-Disposition", "inline; filename=\"fuel.jpg\""));

        verify(receiptService).getReceiptFile(10L, 25L);
    }

    @Test
    void retryReceiptProcessingReturnsAccepted() throws Exception {
        ReimbursementReceipt receipt = receipt(25L, ReceiptProcessingStatus.PENDING);
        when(receiptService.retryReceiptProcessing(10L, 25L))
                .thenReturn(new ReceiptResponse()
                        .setMessage("Receipt processing queued")
                        .setReceipts(List.of(receipt)));

        mockMvc.perform(post("/reimbursements/{reimbursementId}/receipts/{receiptId}/retry", 10L, 25L)
                        .principal(financeAuthentication()))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message").value("Receipt processing queued"));

        verify(receiptService).retryReceiptProcessing(10L, 25L);
    }

    @Test
    void deleteReceiptReturnsDeletedMessage() throws Exception {
        when(receiptService.deleteReceipt(10L, 25L))
                .thenReturn(new ReceiptResponse()
                        .setMessage("Receipt deleted")
                        .setReceipts(List.of()));

        mockMvc.perform(delete("/reimbursements/{reimbursementId}/receipts/{receiptId}", 10L, 25L)
                        .principal(financeAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Receipt deleted"));

        verify(receiptService).deleteReceipt(10L, 25L);
    }

    private UsernamePasswordAuthenticationToken financeAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                99L,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_FINANCE"))
        );
    }

    private ReimbursementReceipt receipt(Long id, ReceiptProcessingStatus status) {
        ReimbursementReceipt receipt = new ReimbursementReceipt();
        receipt.setId(id);
        receipt.setStatus(status);
        return receipt;
    }
}
