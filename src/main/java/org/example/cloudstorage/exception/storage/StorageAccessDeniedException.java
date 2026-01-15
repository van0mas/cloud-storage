package org.example.cloudstorage.exception.storage;

import org.springframework.http.HttpStatus;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.ACCESS_DENIED;

public class StorageAccessDeniedException extends StorageException {
    public  StorageAccessDeniedException(String path, Throwable cause) {
        super(HttpStatus.FORBIDDEN, ACCESS_DENIED, path, cause);
    }
}
