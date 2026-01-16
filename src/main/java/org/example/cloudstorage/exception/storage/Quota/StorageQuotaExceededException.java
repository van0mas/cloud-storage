package org.example.cloudstorage.exception.storage.Quota;

public class StorageQuotaExceededException extends RuntimeException {
    public StorageQuotaExceededException(String message) {
        super(message);
    }
}
