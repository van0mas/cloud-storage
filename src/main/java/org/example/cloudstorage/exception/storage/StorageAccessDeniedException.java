package org.example.cloudstorage.exception.storage;

public class StorageAccessDeniedException extends StorageException {
    public StorageAccessDeniedException(String path, Throwable cause) {
        super("Access denied: " + path, cause);
    }
}
