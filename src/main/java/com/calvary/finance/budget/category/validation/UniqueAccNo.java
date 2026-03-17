package com.calvary.finance.budget.category.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
@Documented
@Constraint(validatedBy = UniqueAccNoValidator.class)
public @interface UniqueAccNo {
    String message() default "accNo already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
