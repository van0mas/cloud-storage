package org.example.cloudstorage.model;

public record StorageResource(
        String fullPath,
        long size
) {
    public boolean isDirectory() {
        return fullPath != null && fullPath.endsWith("/");
    }
}
