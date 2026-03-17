package com.calvary.finance.budget.category.validation;

import com.calvary.finance.budget.category.BudgetCategoryRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueAccNoValidator implements ConstraintValidator<UniqueAccNo, String> {
    private final BudgetCategoryRepository budgetCategoryRepository;

    public UniqueAccNoValidator(BudgetCategoryRepository budgetCategoryRepository) {
        this.budgetCategoryRepository = budgetCategoryRepository;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return false;
        }
        return !budgetCategoryRepository.existsById(value);
    }
}
