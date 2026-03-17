package com.calvary.finance.budget.category.validation;

import com.calvary.finance.budget.category.BudgetCategory;
import com.calvary.finance.budget.category.BudgetCategoryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ExistsAccNoValidator implements ConstraintValidator<ExistsAccNo, String> {
    private final BudgetCategoryRepository budgetCategoryRepository;

    public ExistsAccNoValidator(BudgetCategoryRepository budgetCategoryRepository) {
        this.budgetCategoryRepository = budgetCategoryRepository;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isEmpty()) {
            return false;
        }

        Optional<BudgetCategory> budgetCategory = budgetCategoryRepository.findById(value);

        return budgetCategory.map(BudgetCategory::isActive).orElse(false);
    }
}
