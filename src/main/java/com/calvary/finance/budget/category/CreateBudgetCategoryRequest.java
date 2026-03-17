package com.calvary.finance.budget.category;

import com.calvary.finance.budget.category.validation.UniqueAccNo;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateBudgetCategoryRequest {
    @UniqueAccNo
    private String accNo;

    @NotBlank
    private String description;
}
