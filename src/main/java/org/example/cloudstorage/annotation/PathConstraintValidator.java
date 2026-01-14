package org.example.cloudstorage.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.service.storage.PathValidator;
import org.example.cloudstorage.exception.BadRequestException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PathConstraintValidator implements ConstraintValidator<ValidPath, String> {
    private final PathValidator pathValidator;
    private boolean mustBeDirectory;

    @Override
    public void initialize(ValidPath constraintAnnotation) {
        this.mustBeDirectory = constraintAnnotation.mustBeDirectory();
    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        try {
            pathValidator.validatePath(path, mustBeDirectory);
            return true;
        } catch (BadRequestException e) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(e.getMessage()).addConstraintViolation();
            return false;
        }
    }
}
