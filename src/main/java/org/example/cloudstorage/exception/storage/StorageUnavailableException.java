package org.example.cloudstorage.exception.storage;

import org.springframework.http.HttpStatus;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.STORAGE_UNAVAILABLE;

public class StorageUnavailableException extends StorageException {
    public StorageUnavailableException(String path, Throwable cause) {
        super(HttpStatus.SERVICE_UNAVAILABLE, STORAGE_UNAVAILABLE, path, cause);
    }
}
