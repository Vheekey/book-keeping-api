package com.calvary.finance.budget.category;

import com.calvary.finance.shared.JwtService;
import com.calvary.finance.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BudgetCategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class BudgetCategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BudgetCategoryService budgetCategoryService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private BudgetCategoryRepository budgetCategoryRepository;

    @Test
    void getBudgetCategoryReturnsAllCategories() throws Exception {
        BudgetCategory category = category("1001", "Transport", true);
        when(budgetCategoryService.getAllBudgetCategories())
                .thenReturn(new BudgetCategoryResponse().setCategories(List.of(category)));

        mockMvc.perform(get("/budget/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].accNo").value("1001"))
                .andExpect(jsonPath("$.categories[0].description").value("Transport"))
                .andExpect(jsonPath("$.categories[0].active").value(true));
    }

    @Test
    void getActiveBudgetCategoryReturnsActiveCategories() throws Exception {
        when(budgetCategoryService.getActiveBudgetCategories())
                .thenReturn(new BudgetCategoryResponse().setCategories(List.of(category("1001", "Transport", true))));

        mockMvc.perform(get("/budget/categories/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].active").value(true));
    }

    @Test
    void changeBudgetCategoryActiveStatusPassesPathVariableToService() throws Exception {
        when(budgetCategoryService.changeBudgetCategoryActiveStatus("1001"))
                .thenReturn(new BudgetCategoryResponse().setCategories(List.of(category("1001", "Transport", false))));

        mockMvc.perform(put("/budget/categories/{accNo}/change-status", "1001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].active").value(false));

        verify(budgetCategoryService).changeBudgetCategoryActiveStatus("1001");
    }

    @Test
    void createBudgetCategoryValidatesRequestBody() throws Exception {
        mockMvc.perform(post("/budget/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accNo": "1001",
                                  "description": ""
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors[0].field").value("description"));
    }

    @Test
    void createBudgetCategoryDelegatesValidRequestToService() throws Exception {
        when(budgetCategoryService.createNewBudgetCategory(any(CreateBudgetCategoryRequest.class)))
                .thenReturn(new BudgetCategoryResponse().setCategories(List.of(category("1001", "Transport", true))));

        mockMvc.perform(post("/budget/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accNo": "1001",
                                  "description": "Transport"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories[0].accNo").value("1001"));

        verify(budgetCategoryService).createNewBudgetCategory(any(CreateBudgetCategoryRequest.class));
    }

    private BudgetCategory category(String accNo, String description, boolean active) {
        BudgetCategory category = new BudgetCategory();
        category.setAccNo(accNo);
        category.setDescription(description);
        category.setActive(active);
        return category;
    }
}
