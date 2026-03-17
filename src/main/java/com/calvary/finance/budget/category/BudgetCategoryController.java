package com.calvary.finance.budget.category;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/book-keeping/budget/categories")
public class BudgetCategoryController {
    private final BudgetCategoryService budgetCategoryService;

    public BudgetCategoryController(BudgetCategoryService budgetCategoryService) {
        this.budgetCategoryService = budgetCategoryService;
    }

    @GetMapping
    public ResponseEntity<BudgetCategoryResponse> getBudgetCategory() {
        BudgetCategoryResponse response = budgetCategoryService.getAllBudgetCategories();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/active")
    public ResponseEntity<BudgetCategoryResponse> getActiveBudgetCategory() {
        BudgetCategoryResponse budgetCategoryResponse = budgetCategoryService.getActiveBudgetCategories();
        return ResponseEntity.ok(budgetCategoryResponse);
    }

    @PutMapping("/{accNo}/change-status")
    public ResponseEntity<BudgetCategoryResponse> changeBudgetCategoryActiveStatus(@PathVariable String accNo) {
        BudgetCategoryResponse budgetCategoryResponse = budgetCategoryService.changeBudgetCategoryActiveStatus(accNo);
        return ResponseEntity.ok(budgetCategoryResponse);
    }

    @PostMapping
    public ResponseEntity<BudgetCategoryResponse> createBudgetCategory(
            @Valid @RequestBody CreateBudgetCategoryRequest createBudgetCategoryRequest
    ) {
        BudgetCategoryResponse budgetCategoryResponse = budgetCategoryService.createNewBudgetCategory(createBudgetCategoryRequest);
        return ResponseEntity.ok(budgetCategoryResponse);
    }
}
