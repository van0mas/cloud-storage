package org.example.cloudstorage.advice;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.config.AppConstants;
import org.example.cloudstorage.exception.storage.Quota.StorageQuotaExceededException;
import org.example.cloudstorage.exception.storage.StorageException;
import org.example.cloudstorage.exception.user.UnauthorizedException;
import org.example.cloudstorage.exception.user.UserAlreadyExistsException;
import org.example.cloudstorage.exception.user.UserNotFoundException;
import org.example.cloudstorage.exception.BadRequestException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErrorResponse(String message) {}

    // Storage
    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(StorageException e) {
        String relativePath = PathUtils.extractRelativePath(AppConstants.Storage.USER_PREFIX_PATTERN, e.getPath());
        String fullMessage = e.getMessage() + AppConstants.ExceptionMessages.MESSAGE_PATH_DELIMITER + relativePath;

        log.warn("Storage exception occurred: status={}, message='{}', path='{}'",
                e.getHttpStatus(), e.getMessage(), relativePath);

        return ResponseEntity.status(e.getHttpStatus())
                .body(new ErrorResponse(fullMessage));
    }

    // 400
    @ExceptionHandler({
            BadRequestException.class,
            IllegalArgumentException.class,
            ConstraintViolationException.class,
            MethodArgumentNotValidException.class
    })
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception e) {
        String message = e.getMessage();

        if (e instanceof MethodArgumentNotValidException ex) {
            message = ex.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        }
        else if (e instanceof ConstraintViolationException ex) {
            message = ex.getConstraintViolations().iterator().next().getMessage();
        }

        log.warn("Bad request: {}", message);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(message));
    }

    // 401
    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleUnauthorized(RuntimeException e) {
        String msg = (e instanceof BadCredentialsException)
                ? AppConstants.ExceptionMessages.BAD_CREDENTIALS
                : e.getMessage();

        log.warn("Unauthorized access attempt: {}", msg);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(msg));
    }

    // 404
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException e) {
        log.warn("User not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 409
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleConflict(UserAlreadyExistsException e) {
        log.warn("User conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 413
    @ExceptionHandler(StorageQuotaExceededException.class)
    public ResponseEntity<ErrorResponse> handleQuotaExceeded(StorageQuotaExceededException e) {
        log.warn("Storage quota exceeded: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ErrorResponse(e.getMessage()));
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleInternalError(Exception e) {
        log.error("UNEXPECTED INTERNAL ERROR: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(AppConstants.ExceptionMessages.INTERNAL_SERVER_ERROR));
    }
}
