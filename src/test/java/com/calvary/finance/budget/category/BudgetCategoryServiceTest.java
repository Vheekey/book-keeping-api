package com.calvary.finance.budget.category;

import com.calvary.finance.audit.AuditLogService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BudgetCategoryServiceTest {

    @Mock
    private BudgetCategoryRepository budgetCategoryRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private BudgetCategoryService budgetCategoryService;

    @Test
    void createNewBudgetCategorySavesActiveCategoryAndWritesAuditLog() {
        CreateBudgetCategoryRequest request = new CreateBudgetCategoryRequest();
        request.setAccNo("1001");
        request.setDescription("Transport");

        when(budgetCategoryRepository.save(any(BudgetCategory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BudgetCategoryResponse response = budgetCategoryService.createNewBudgetCategory(request);

        ArgumentCaptor<BudgetCategory> categoryCaptor = ArgumentCaptor.forClass(BudgetCategory.class);
        verify(budgetCategoryRepository).save(categoryCaptor.capture());
        BudgetCategory savedCategory = categoryCaptor.getValue();

        assertThat(savedCategory.getAccNo()).isEqualTo("1001");
        assertThat(savedCategory.getDescription()).isEqualTo("Transport");
        assertThat(savedCategory.isActive()).isTrue();
        assertThat(response.getCategories()).containsExactly(savedCategory);

        verify(auditLogService).log(
                eq("BUDGET_CATEGORY_CREATED"),
                eq("BudgetCategory"),
                eq("1001"),
                any(Map.class)
        );
    }

    @Test
    void changeBudgetCategoryActiveStatusTogglesStatusAndWritesAuditLog() {
        BudgetCategory category = new BudgetCategory();
        category.setAccNo("1001");
        category.setDescription("Transport");
        category.setActive(true);

        when(budgetCategoryRepository.findById("1001")).thenReturn(Optional.of(category));
        when(budgetCategoryRepository.save(category)).thenReturn(category);

        BudgetCategoryResponse response = budgetCategoryService.changeBudgetCategoryActiveStatus("1001");

        assertThat(category.isActive()).isFalse();
        assertThat(response.getCategories()).containsExactly(category);
        verify(auditLogService).log(
                eq("BUDGET_CATEGORY_STATUS_CHANGED"),
                eq("BudgetCategory"),
                eq("1001"),
                eq(Map.of("isActive", false))
        );
    }

    @Test
    void changeBudgetCategoryActiveStatusThrowsWhenCategoryDoesNotExist() {
        when(budgetCategoryRepository.findById("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> budgetCategoryService.changeBudgetCategoryActiveStatus("missing"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Budget Category not found: missing");
    }

    @Test
    void getActiveBudgetCategoriesReturnsRepositoryResults() {
        BudgetCategory category = new BudgetCategory();
        category.setAccNo("1001");
        category.setActive(true);
        when(budgetCategoryRepository.findByIsActiveTrue()).thenReturn(List.of(category));

        BudgetCategoryResponse response = budgetCategoryService.getActiveBudgetCategories();

        assertThat(response.getCategories()).containsExactly(category);
    }
}
