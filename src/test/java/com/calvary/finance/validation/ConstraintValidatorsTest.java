package com.calvary.finance.validation;

import com.calvary.finance.budget.category.BudgetCategory;
import com.calvary.finance.budget.category.BudgetCategoryRepository;
import com.calvary.finance.budget.category.validation.ExistsAccNoValidator;
import com.calvary.finance.budget.category.validation.UniqueAccNoValidator;
import com.calvary.finance.user.repository.UserAuthProviderRepository;
import com.calvary.finance.user.repository.UserRepository;
import com.calvary.finance.user.request.validation.UniqueEmailValidator;
import com.calvary.finance.user.request.validation.UniqueUsernameValidator;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConstraintValidatorsTest {

    @Test
    void uniqueEmailValidatorRejectsNullOrBlankAndExistingEmail() {
        UserRepository userRepository = mock(UserRepository.class);
        UniqueEmailValidator validator = new UniqueEmailValidator(userRepository);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);

        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid("   ", null)).isFalse();
        assertThat(validator.isValid("john@example.com", null)).isFalse();
        assertThat(validator.isValid("new@example.com", null)).isTrue();
    }

    @Test
    void uniqueUsernameValidatorAllowsBlankButRejectsExistingUsername() {
        UserAuthProviderRepository repository = mock(UserAuthProviderRepository.class);
        UniqueUsernameValidator validator = new UniqueUsernameValidator(repository);
        when(repository.existsUserAuthProviderByUsername("jane")).thenReturn(true);
        when(repository.existsUserAuthProviderByUsername("new-user")).thenReturn(false);

        assertThat(validator.isValid(null, null)).isTrue();
        assertThat(validator.isValid(" ", null)).isTrue();
        assertThat(validator.isValid("jane", null)).isFalse();
        assertThat(validator.isValid("new-user", null)).isTrue();
    }

    @Test
    void uniqueAccNoValidatorRejectsNullOrBlankAndExistingAccountNumber() {
        BudgetCategoryRepository repository = mock(BudgetCategoryRepository.class);
        UniqueAccNoValidator validator = new UniqueAccNoValidator(repository);
        when(repository.existsById("1001")).thenReturn(true);
        when(repository.existsById("2002")).thenReturn(false);

        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid("  ", null)).isFalse();
        assertThat(validator.isValid("1001", null)).isFalse();
        assertThat(validator.isValid("2002", null)).isTrue();
    }

    @Test
    void existsAccNoValidatorRequiresActiveBudgetCategory() {
        BudgetCategoryRepository repository = mock(BudgetCategoryRepository.class);
        ExistsAccNoValidator validator = new ExistsAccNoValidator(repository);
        BudgetCategory active = new BudgetCategory();
        active.setActive(true);
        BudgetCategory inactive = new BudgetCategory();
        inactive.setActive(false);
        when(repository.findById("1001")).thenReturn(Optional.of(active));
        when(repository.findById("2002")).thenReturn(Optional.of(inactive));
        when(repository.findById("3003")).thenReturn(Optional.empty());

        assertThat(validator.isValid(null, null)).isFalse();
        assertThat(validator.isValid("", null)).isFalse();
        assertThat(validator.isValid("1001", null)).isTrue();
        assertThat(validator.isValid("2002", null)).isFalse();
        assertThat(validator.isValid("3003", null)).isFalse();
    }
}
