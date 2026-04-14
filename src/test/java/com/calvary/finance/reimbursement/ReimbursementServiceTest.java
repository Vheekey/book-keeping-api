package com.calvary.finance.reimbursement;

import com.calvary.finance.audit.AuditLogService;
import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReimbursementServiceTest {

    @Mock
    private ReimbursementRepository reimbursementRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private ReimbursementService reimbursementService;

    @Test
    void createNewReimbursementMapsRequestSetsPendingStatusAndWritesAuditLog() {
        CreateReimbursementRequest request = createRequest();

        when(reimbursementRepository.save(any(Reimbursement.class))).thenAnswer(invocation -> {
            Reimbursement reimbursement = invocation.getArgument(0);
            reimbursement.setId(25L);
            return reimbursement;
        });

        ReimbursementResponse response = reimbursementService.createNewReimbursement(request);

        ArgumentCaptor<Reimbursement> reimbursementCaptor = ArgumentCaptor.forClass(Reimbursement.class);
        verify(reimbursementRepository).save(reimbursementCaptor.capture());
        Reimbursement saved = reimbursementCaptor.getValue();

        assertThat(saved.getAmount()).isEqualByComparingTo("150.50");
        assertThat(saved.getDescription()).isEqualTo("Fuel reimbursement");
        assertThat(saved.getExpenditureDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(saved.getStatus()).isEqualTo(ReimbursementStatus.PENDING);
        assertThat(saved.isCorrect()).isTrue();
        assertThat(response.getMessage()).isEqualTo("Reimbursement created");
        assertThat(response.getReimbursements()).containsExactly(saved);

        verify(auditLogService).log(
                eq("REIMBURSEMENT_CREATED"),
                eq("Reimbursement"),
                eq("25"),
                eq("John Doe"),
                any(Map.class)
        );
    }

    @Test
    void approveNewReimbursementMarksApprovedAndStoresProcessorDetails() {
        Reimbursement reimbursement = reimbursement(10L, ReimbursementStatus.PENDING);
        when(reimbursementRepository.findById(10L)).thenReturn(Optional.of(reimbursement));
        when(reimbursementRepository.save(reimbursement)).thenReturn(reimbursement);

        ReimbursementResponse response = reimbursementService.approveNewReimbursement(
                10L,
                99L,
                "Approved",
                true
        );

        assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementStatus.APPROVED);
        assertThat(reimbursement.getProcessedBy()).isEqualTo(99L);
        assertThat(reimbursement.getProcessedAt()).isNotNull();
        assertThat(reimbursement.getAdminComment()).isEqualTo("Approved");
        assertThat(response.getMessage()).isEqualTo("Reimbursement Processed");
        verify(auditLogService).log(
                eq("REIMBURSEMENT_APPROVAL_DECISION"),
                eq("Reimbursement"),
                eq("10"),
                eq("99"),
                any(Map.class)
        );
    }

    @Test
    void approveNewReimbursementMarksRejectedWhenNotApproved() {
        Reimbursement reimbursement = reimbursement(10L, ReimbursementStatus.PENDING);
        when(reimbursementRepository.findById(10L)).thenReturn(Optional.of(reimbursement));
        when(reimbursementRepository.save(reimbursement)).thenReturn(reimbursement);

        reimbursementService.approveNewReimbursement(10L, 99L, "Rejected", false);

        assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementStatus.REJECTED);
    }

    @Test
    void payoutReimbursementRequiresApprovedStatus() {
        Reimbursement reimbursement = reimbursement(10L, ReimbursementStatus.PENDING);
        when(reimbursementRepository.findById(10L)).thenReturn(Optional.of(reimbursement));

        assertThatThrownBy(() -> reimbursementService.payoutReimbursement(10L, 99L))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Reimbursement not approved for payout");

        verify(reimbursementRepository, never()).save(any(Reimbursement.class));
    }

    @Test
    void payoutReimbursementMarksApprovedReimbursementPaid() {
        Reimbursement reimbursement = reimbursement(10L, ReimbursementStatus.APPROVED);
        when(reimbursementRepository.findById(10L)).thenReturn(Optional.of(reimbursement));
        when(reimbursementRepository.save(reimbursement)).thenReturn(reimbursement);

        ReimbursementResponse response = reimbursementService.payoutReimbursement(10L, 99L);

        assertThat(reimbursement.getStatus()).isEqualTo(ReimbursementStatus.PAID);
        assertThat(reimbursement.getPaidOutBy()).isEqualTo(99L);
        assertThat(reimbursement.getPaidOutAt()).isNotNull();
        assertThat(response.getMessage()).isEqualTo("Reimbursement paid");
        verify(auditLogService).log(
                eq("REIMBURSEMENT_PAID"),
                eq("Reimbursement"),
                eq("10"),
                eq("99"),
                any(Map.class)
        );
    }

    @Test
    void getReimbursementThrowsWhenMissing() {
        when(reimbursementRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reimbursementService.getReimbursement(404L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Reimbursement not found");
    }

    @Test
    void getReimbursementsUsesStatusAndDateRangeFilter() {
        LocalDate startDate = LocalDate.of(2026, 4, 1);
        LocalDate endDate = LocalDate.of(2026, 4, 30);
        Reimbursement reimbursement = reimbursement(10L, ReimbursementStatus.APPROVED);
        when(reimbursementRepository.findAllByStatusAndExpenditureDateBetween(
                eq(ReimbursementStatus.APPROVED),
                eq(startDate),
                eq(endDate),
                any(Pageable.class)
        )).thenReturn(new PageImpl<>(List.of(reimbursement)));

        ReimbursementResponse response = reimbursementService.getReimbursements(
                0,
                10,
                "approved",
                startDate,
                endDate
        );

        assertThat(response.getReimbursements()).containsExactly(reimbursement);
        assertThat(response.getMessage()).isEqualTo("All Reimbursements");
    }

    private CreateReimbursementRequest createRequest() {
        CreateReimbursementRequest request = new CreateReimbursementRequest();
        request.setAmount(new BigDecimal("150.50"));
        request.setDescription("Fuel reimbursement");
        request.setExpenditureDate(LocalDate.of(2026, 4, 1));
        request.setName("John Doe");
        request.setShouldReimburse(true);
        request.setAccountName("John Doe");
        request.setClearingNumber("1234");
        request.setAccountNumber("1234567890");
        request.setAccNo("1001");
        request.setPhoneNumber("+233123456789");
        request.setIsCorrect(true);
        return request;
    }

    private Reimbursement reimbursement(Long id, ReimbursementStatus status) {
        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setId(id);
        reimbursement.setName("John Doe");
        reimbursement.setDescription("Fuel reimbursement");
        reimbursement.setAmount(new BigDecimal("150.50"));
        reimbursement.setExpenditureDate(LocalDate.of(2026, 4, 1));
        reimbursement.setShouldReimburse(true);
        reimbursement.setAccNo("1001");
        reimbursement.setCorrect(true);
        reimbursement.setStatus(status);
        return reimbursement;
    }
}
