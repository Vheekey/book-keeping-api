package com.calvary.finance.budget.category;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class BudgetCategoryResponse {
    private List<BudgetCategory> categories;
}
