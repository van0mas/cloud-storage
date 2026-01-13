package org.example.cloudstorage.exception.storage;

public class ParentNotFoundException extends StorageException{
    public ParentNotFoundException(String message) {
        super("Родительская папка не существует: " + message);
    }
}
