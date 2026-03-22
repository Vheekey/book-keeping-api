package com.calvary.finance.budget.category;

import com.calvary.finance.audit.AuditLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BudgetCategoryService {
    private final BudgetCategoryRepository budgetCategoryRepository;
    private final AuditLogService auditLogService;

    public BudgetCategoryService(
            BudgetCategoryRepository budgetCategoryRepository,
            AuditLogService auditLogService
    ) {
        this.budgetCategoryRepository = budgetCategoryRepository;
        this.auditLogService = auditLogService;
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

    @Transactional
    public BudgetCategoryResponse changeBudgetCategoryActiveStatus(String accNo) {
        BudgetCategory budgetCategory = budgetCategoryRepository.findById(accNo)
                .orElseThrow(() -> new IllegalArgumentException("Budget Category not found: " + accNo));
        budgetCategory.setActive(!budgetCategory.isActive());
        BudgetCategory saved = budgetCategoryRepository.save(budgetCategory);
        //TODO: Add authenticated user as AUDIT ACTOR
        auditLogService.log(
                "BUDGET_CATEGORY_STATUS_CHANGED",
                "BudgetCategory",
                saved.getAccNo(),
                Map.of("isActive", saved.isActive())
        );

        return new BudgetCategoryResponse().setCategories(List.of(saved));
    }

    @Transactional
    public BudgetCategoryResponse createNewBudgetCategory(CreateBudgetCategoryRequest createBudgetCategoryRequest) {
        BudgetCategory budgetCategory = new BudgetCategory();
        budgetCategory.setAccNo(createBudgetCategoryRequest.getAccNo());
        budgetCategory.setDescription(createBudgetCategoryRequest.getDescription());
        budgetCategory.setActive(true);

        BudgetCategory newCategory = budgetCategoryRepository.save(budgetCategory);
        //TODO: Add authenticated user as AUDIT ACTOR
        auditLogService.log(
                "BUDGET_CATEGORY_CREATED",
                "BudgetCategory",
                newCategory.getAccNo(),
                buildAuditDetails(newCategory)
        );
        return new BudgetCategoryResponse().setCategories(List.of(newCategory));
    }

    private Map<String, Object> buildAuditDetails(BudgetCategory budgetCategory) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("accNo", budgetCategory.getAccNo());
        details.put("description", budgetCategory.getDescription());
        details.put("isActive", budgetCategory.isActive());
        return details;
    }
}
