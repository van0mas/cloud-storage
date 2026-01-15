package org.example.cloudstorage.exception.storage;

import org.springframework.http.HttpStatus;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.CONFLICT;

public class StorageConflictException extends StorageException {
    public StorageConflictException(String path) {
        super(HttpStatus.CONFLICT, CONFLICT, path);
    }
}