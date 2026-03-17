package com.calvary.finance.budget.category;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BudgetCategoryService {
    private final BudgetCategoryRepository budgetCategoryRepository;

    public BudgetCategoryService(BudgetCategoryRepository budgetCategoryRepository) {
        this.budgetCategoryRepository = budgetCategoryRepository;
    }

    public BudgetCategoryResponse getAllBudgetCategories() {
        List<BudgetCategory> categories = budgetCategoryRepository.findAll();
        BudgetCategoryResponse response = new BudgetCategoryResponse();
        response.setCategories(categories);

        return response;
    }

    public BudgetCategoryResponse getActiveBudgetCategories() {
        List<BudgetCategory> activeCategories = budgetCategoryRepository.findByIsActiveTrue();

        return new BudgetCategoryResponse().setCategories(activeCategories);
    }

    public BudgetCategoryResponse changeBudgetCategoryActiveStatus(String accNo) {
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(accNo)
                .orElseThrow(() -> new IllegalArgumentException("Budget Category not found: " + accNo));
        budgetCategory.setActive(!budgetCategory.isActive());
        BudgetCategory saved = budgetCategoryRepository.save(budgetCategory);

        return new BudgetCategoryResponse().setCategories(List.of(saved));
    }

    public BudgetCategoryResponse createNewBudgetCategory(CreateBudgetCategoryRequest createBudgetCategoryRequest) {
        BudgetCategory budgetCategory = new BudgetCategory();
        budgetCategory.setAccNo(createBudgetCategoryRequest.getAccNo());
        budgetCategory.setDescription(createBudgetCategoryRequest.getDescription());
        budgetCategory.setActive(true);

        BudgetCategory newCategory = budgetCategoryRepository.save(budgetCategory);
        return new BudgetCategoryResponse().setCategories(List.of(newCategory));
    }
}
