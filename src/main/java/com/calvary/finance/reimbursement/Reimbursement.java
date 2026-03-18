package com.calvary.finance.reimbursement;

import com.calvary.finance.budget.category.validation.ExistsAccNo;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reimbursements")
@NoArgsConstructor
@Data
public class Reimbursement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String description;
    private BigDecimal amount;
    private LocalDate expenditureDate;
    private String name;
    private boolean shouldReimburse;
    private String accountName;
    private String clearingNumber;
    private String accountNumber;
    private String accNo;
    private String phoneNumber;
    private boolean isCorrect;
}
