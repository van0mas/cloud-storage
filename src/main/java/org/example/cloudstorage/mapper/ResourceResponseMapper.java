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
        String fullPath = resource.fullPath();
        String relativePath = PathUtils.extractRelativePath(AppConstants.Storage.USER_PREFIX_PATTERN, fullPath);
        String resourceName = PathUtils.extractName(relativePath);
        String parentPath = PathUtils.extractParentPath(relativePath);

        if (resource.isDirectory()) {
            // фронт требует, чтобы имя папки заканчивалось слэшем
            String dirName = resourceName + "/";
            return ResourceInfoDto.directory(parentPath, dirName);
        } else {
            return ResourceInfoDto.file(parentPath, resourceName, resource.size());
        }
    }

    public List<ResourceInfoDto> toDtoList(List<StorageResource> resources) {
        if (resources == null) {
            return List.of();
        }
        return resources.stream()
                .map(this::toDto)
                .toList();
    }
}