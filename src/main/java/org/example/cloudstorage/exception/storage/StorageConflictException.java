package org.example.cloudstorage.exception.storage;

public class StorageConflictException extends StorageException {
    public StorageConflictException(String path) {
        super("Target path already occupied: " + path, null);
    }
}