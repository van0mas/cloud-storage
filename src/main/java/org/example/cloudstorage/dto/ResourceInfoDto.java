package org.example.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResourceInfoDto(
        String path,
        String name,
        Long size,
        ResourceType type
) {
    public enum ResourceType {
        FILE, DIRECTORY
    }

    public static ResourceInfoDto file(String path, String name, long size) {
        return new ResourceInfoDto(path, name, size, ResourceType.FILE);
    }

    public static ResourceInfoDto directory(String path, String name) {
        return new ResourceInfoDto(path, name, null, ResourceType.DIRECTORY);
    }
}
