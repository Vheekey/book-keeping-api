package com.calvary.finance.user.request.validation;

import com.calvary.finance.user.repository.UserAuthProviderRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

@Component
public class UniqueUsernameValidator implements ConstraintValidator<UniqueUsername, String> {
    private final UserAuthProviderRepository userAuthProviderRepository;

    public UniqueUsernameValidator(UserAuthProviderRepository userAuthProviderRepository) {
        this.userAuthProviderRepository = userAuthProviderRepository;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !userAuthProviderRepository.existsUserAuthProviderByUsername(value);
    }
}
