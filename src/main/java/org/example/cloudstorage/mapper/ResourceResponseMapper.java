package org.example.cloudstorage.mapper;

import org.example.cloudstorage.config.AppConstants;
import org.example.cloudstorage.dto.ResourceInfoDto;
import org.example.cloudstorage.model.StorageResource;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ResourceResponseMapper {

    public ResourceInfoDto toDto(StorageResource resource) {
        return toDto(resource.fullPath(), resource.isDirectory(), resource.size());
    }

    public ResourceInfoDto fromDirectory(String fullPath) {
        return toDto(fullPath, true, null);
    }

    private ResourceInfoDto toDto(String fullPath, boolean isDirectory, Long size) {
        String relativePath = PathUtils.extractRelativePath(AppConstants.Storage.USER_PREFIX_PATTERN, fullPath);
        String resourceName = PathUtils.extractName(relativePath);
        String parentPath = PathUtils.extractParentPath(relativePath);

        if (isDirectory) {
            // фронт требует, чтобы имя папки заканчивалось слэшем
            String dirName = resourceName.endsWith("/") ? resourceName : resourceName + "/";
            return ResourceInfoDto.directory(parentPath, dirName);
        } else {
            return ResourceInfoDto.file(parentPath, resourceName, size);
        }
    }

    public List<ResourceInfoDto> toDtoList(List<StorageResource> resources) {
        return resources == null ? List.of() : resources.stream().map(this::toDto).toList();
    }
}