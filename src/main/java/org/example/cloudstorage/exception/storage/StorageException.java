package org.example.cloudstorage.exception.storage;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class StorageException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String path;

    protected StorageException(HttpStatus httpStatus, String message, String path) {
        super(message);
        this.httpStatus = httpStatus;
        this.path = path;
    }

    protected StorageException(HttpStatus httpStatus, String message, String path, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.path = path;
    }
}
