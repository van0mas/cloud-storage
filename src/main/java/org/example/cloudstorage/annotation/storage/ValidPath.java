package org.example.cloudstorage.annotation.storage;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.INVALID_INPUT;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PathConstraintValidator.class)
@Documented
public @interface ValidPath {
    String message() default INVALID_INPUT;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    boolean mustBeDirectory() default false;
}
