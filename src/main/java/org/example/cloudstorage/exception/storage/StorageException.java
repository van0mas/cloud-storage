package org.example.cloudstorage.exception.storage;

public abstract class StorageException extends RuntimeException {
    protected StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
