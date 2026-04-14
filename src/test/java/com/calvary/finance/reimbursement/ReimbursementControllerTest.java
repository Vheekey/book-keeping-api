package com.calvary.finance.reimbursement;

import com.calvary.finance.budget.category.BudgetCategoryRepository;
import com.calvary.finance.reimbursement.requests.CreateReimbursementRequest;
import com.calvary.finance.shared.JwtService;
import com.calvary.finance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ReimbursementController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReimbursementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReimbursementService reimbursementService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BudgetCategoryRepository budgetCategoryRepository;

    @Test
    void createReimbursementReturnsCreatedReimbursement() throws Exception {
        when(budgetCategoryRepository.findById("1001"))
                .thenReturn(java.util.Optional.of(activeBudgetCategory("1001")));
        when(reimbursementService.createNewReimbursement(any(CreateReimbursementRequest.class)))
                .thenReturn(new ReimbursementResponse()
                        .setMessage("Reimbursement created")
                        .setReimbursements(List.of(reimbursement(10L, ReimbursementStatus.PENDING))));

        mockMvc.perform(post("/reimbursements/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validCreatePayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reimbursement created"))
                .andExpect(jsonPath("$.reimbursements[0].id").value(10))
                .andExpect(jsonPath("$.reimbursements[0].status").value("PENDING"));
    }

    @Test
    void createReimbursementValidatesInvalidRequestBody() throws Exception {
        mockMvc.perform(post("/reimbursements/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "expenditureDate": "2026-04-01",
                                  "name": "",
                                  "description": "Fuel reimbursement",
                                  "amount": 0,
                                  "shouldReimburse": true,
                                  "accountName": "John Doe",
                                  "clearingNumber": "12",
                                  "accountNumber": "123",
                                  "accNo": "1001",
                                  "phoneNumber": "abc",
                                  "isCorrect": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }

    @Test
    void getReimbursementsPassesQueryParametersToService() throws Exception {
        when(reimbursementService.getReimbursements(
                2,
                25,
                "approved",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        )).thenReturn(new ReimbursementResponse()
                .setMessage("All Reimbursements")
                .setReimbursements(List.of(reimbursement(10L, ReimbursementStatus.APPROVED))));

        mockMvc.perform(get("/reimbursements")
                        .param("pageNumber", "2")
                        .param("pageSize", "25")
                        .param("status", "approved")
                        .param("startDate", "2026-04-01")
                        .param("endDate", "2026-04-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reimbursements[0].status").value("APPROVED"));

        verify(reimbursementService).getReimbursements(
                2,
                25,
                "approved",
                LocalDate.of(2026, 4, 1),
                LocalDate.of(2026, 4, 30)
        );
    }

    @Test
    void getReimbursementPassesPathVariableToService() throws Exception {
        when(reimbursementService.getReimbursement(10L))
                .thenReturn(new ReimbursementResponse()
                        .setMessage("Reimbursement retrieved")
                        .setReimbursements(List.of(reimbursement(10L, ReimbursementStatus.PENDING))));

        mockMvc.perform(get("/reimbursements/{reimbursementId}", 10L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reimbursement retrieved"));

        verify(reimbursementService).getReimbursement(10L);
    }

    @Test
    void approveReimbursementUsesAuthenticatedPrincipalAndRequestBody() throws Exception {
        when(reimbursementService.approveNewReimbursement(10L, 99L, "Approved", true))
                .thenReturn(new ReimbursementResponse()
                        .setMessage("Reimbursement Processed")
                        .setReimbursements(List.of(reimbursement(10L, ReimbursementStatus.APPROVED))));

        mockMvc.perform(post("/reimbursements/{reimbursementId}/approve", 10L)
                        .principal(financeAuthentication())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "comment": "Approved",
                                  "isApproved": "true"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reimbursement Processed"));

        verify(reimbursementService).approveNewReimbursement(10L, 99L, "Approved", true);
    }

    @Test
    void payoutReimbursementUsesAuthenticatedPrincipal() throws Exception {
        when(reimbursementService.payoutReimbursement(10L, 99L))
                .thenReturn(new ReimbursementResponse()
                        .setMessage("Reimbursement paid")
                        .setReimbursements(List.of(reimbursement(10L, ReimbursementStatus.PAID))));

        mockMvc.perform(post("/reimbursements/{reimbursementId}/payout", 10L)
                        .principal(financeAuthentication()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Reimbursement paid"));

        verify(reimbursementService).payoutReimbursement(10L, 99L);
    }

    private UsernamePasswordAuthenticationToken financeAuthentication() {
        return new UsernamePasswordAuthenticationToken(
                99L,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_FINANCE"))
        );
    }

    private String validCreatePayload() {
        return """
                {
                  "expenditureDate": "2026-04-01",
                  "name": "John Doe",
                  "description": "Fuel reimbursement",
                  "amount": 150.50,
                  "shouldReimburse": true,
                  "accountName": "John Doe",
                  "clearingNumber": "1234",
                  "accountNumber": "1234567890",
                  "accNo": "1001",
                  "phoneNumber": "+233123456789",
                  "isCorrect": true
                }
                """;
    }

    private Reimbursement reimbursement(Long id, ReimbursementStatus status) {
        Reimbursement reimbursement = new Reimbursement();
        reimbursement.setId(id);
        reimbursement.setName("John Doe");
        reimbursement.setDescription("Fuel reimbursement");
        reimbursement.setAmount(new BigDecimal("150.50"));
        reimbursement.setExpenditureDate(LocalDate.of(2026, 4, 1));
        reimbursement.setStatus(status);
        return reimbursement;
    }

    private com.calvary.finance.budget.category.BudgetCategory activeBudgetCategory(String accNo) {
        com.calvary.finance.budget.category.BudgetCategory category =
                new com.calvary.finance.budget.category.BudgetCategory();
        category.setAccNo(accNo);
        category.setDescription("Transport");
        category.setActive(true);
        return category;
    }
}
