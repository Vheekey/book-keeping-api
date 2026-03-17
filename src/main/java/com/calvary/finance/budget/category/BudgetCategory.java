package com.calvary.finance.budget.category;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "budget_categories")
@Data
@NoArgsConstructor
public class BudgetCategory {
    @Id
    private String accNo;
    private String description;
    private boolean isActive;
}
