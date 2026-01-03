package org.example.cloudstorage.exception.storage;

public class StorageUnavailableException extends StorageException {
    public StorageUnavailableException(Throwable cause) {
        super("Object storage unavailable", cause);
    }
}
