package org.example.cloudstorage.exception.storage;

public class StorageNotFoundException extends StorageException {
    public StorageNotFoundException(String path, Throwable cause) {
        super("Resource not found: " + path, cause);
    }
}
