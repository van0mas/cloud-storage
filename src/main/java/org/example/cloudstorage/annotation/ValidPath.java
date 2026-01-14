package org.example.cloudstorage.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PathConstraintValidator.class)
@Documented
public @interface ValidPath {
    String message() default "Невалидный путь";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    boolean mustBeDirectory() default false;
}
