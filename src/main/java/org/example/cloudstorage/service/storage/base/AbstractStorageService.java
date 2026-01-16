package org.example.cloudstorage.service.storage.base;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.config.AppConstants;
import org.example.cloudstorage.mapper.ResourceResponseMapper;
import org.example.cloudstorage.service.storage.port.ObjectStoragePort;
import org.example.cloudstorage.service.storage.validation.PathValidator;
import org.example.cloudstorage.util.PathUtils;

@RequiredArgsConstructor
public abstract class AbstractStorageService {
    protected final ObjectStoragePort storagePort;
    protected final PathValidator pathValidator;
    protected final ResourceResponseMapper resourceMapper;

    protected String generateUserPath(long userId, String path) {
        return generateUserPrefix(userId) + PathUtils.normalize(path);
    }

    protected String generateUserPrefix(long userId) {
        return AppConstants.Storage.USER_ROOT_TEMPLATE.formatted(userId);
    }
}