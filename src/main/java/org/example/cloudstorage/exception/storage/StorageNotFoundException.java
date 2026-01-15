package org.example.cloudstorage.exception.storage;

import org.springframework.http.HttpStatus;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.RESOURCE_NOT_FOUND;

public class StorageNotFoundException extends StorageException {
    public StorageNotFoundException(String path) {
        super(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND, path);
    }

    public StorageNotFoundException(String path, Throwable cause) {
        super(HttpStatus.NOT_FOUND, RESOURCE_NOT_FOUND, path, cause);
    }
}
