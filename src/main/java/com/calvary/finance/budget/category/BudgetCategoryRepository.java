package com.calvary.finance.budget.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetCategoryRepository extends JpaRepository<BudgetCategory, String> {
    List<BudgetCategory> findByIsActiveTrue();
}
