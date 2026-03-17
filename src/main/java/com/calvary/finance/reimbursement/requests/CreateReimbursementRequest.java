package com.calvary.finance.reimbursement.requests;

import com.calvary.finance.budget.category.validation.ExistsAccNo;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CreateReimbursementRequest {
    @PastOrPresent
    @NotNull
    private LocalDate expenditureDate;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal amount;

    @NotNull
    private Boolean shouldReimburse;

    @NotBlank
    private String accountName;

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "Account number must be 4 digits")
    private String clearingNumber;

    @NotBlank
    @Pattern(regexp = "\\d{10,20}", message = "Account number must be 10–20 digits")
    private String accountNumber;

    @ExistsAccNo
    private String accNo;

    @NotBlank
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Phone number must be 10–15 digits, optional leading +")
    private String phoneNumber;

    @AssertTrue(message = "Confirm that details is correct")
    @NotNull
    private Boolean isCorrect;
}
