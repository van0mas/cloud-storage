package org.example.cloudstorage.exception.storage;

import org.springframework.http.HttpStatus;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.STORAGE_ERROR;

public class StorageInternalException extends StorageException {
    public StorageInternalException(String path, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, STORAGE_ERROR, path, cause);
    }
}
