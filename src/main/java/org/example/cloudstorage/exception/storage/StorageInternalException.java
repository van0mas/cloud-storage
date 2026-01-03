package org.example.cloudstorage.exception.storage;

public class StorageInternalException extends StorageException {
    public StorageInternalException(Throwable cause) {
        super("Internal storage error", cause);
    }
}
