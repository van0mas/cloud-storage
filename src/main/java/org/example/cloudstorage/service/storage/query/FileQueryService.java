package org.example.cloudstorage.service.storage.query;

import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.ResourceInfoDto;
import org.example.cloudstorage.mapper.ResourceResponseMapper;
import org.example.cloudstorage.service.storage.validation.PathValidator;
import org.example.cloudstorage.service.storage.base.AbstractStorageService;
import org.example.cloudstorage.service.storage.port.ObjectStoragePort;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class FileQueryService extends AbstractStorageService {

    public FileQueryService(ObjectStoragePort storagePort, PathValidator pathValidator,
                            ResourceResponseMapper resourceMapper) {
        super(storagePort, pathValidator, resourceMapper);
    }

    public ResourceInfoDto getResource(long userId, String path) {
        log.info("Request to get resource: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);
        log.debug("Full path for getResource: '{}'", fullPath);
        return resourceMapper.toDto(storagePort.getResource(fullPath));
    }

    public List<ResourceInfoDto> search(long userId, String query) {
        log.info("Search request: userId={}, query='{}'", userId, query);
        String prefix = generateUserPrefix(userId);
        var foundResources = storagePort.listAllObjectsRecursive(prefix);

        var result = foundResources.stream()
                .filter(res -> res.fullPath().toLowerCase().contains(query.toLowerCase()))
                .toList();

        log.info("Search found {} items for query '{}'", result.size(), query);
        return resourceMapper.toDtoList(result);
    }

    public List<ResourceInfoDto> listFolder(long userId, String path) {
        log.info("List folder request: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);
        var resources = storagePort.listFolder(fullPath);

        return resourceMapper.toDtoList(resources.stream()
                .filter(resource -> !resource.fullPath().equals(fullPath)).toList());
    }
}
