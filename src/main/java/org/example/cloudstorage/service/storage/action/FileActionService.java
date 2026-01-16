package org.example.cloudstorage.service.storage.action;

import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.ResourceInfoDto;
import org.example.cloudstorage.mapper.ResourceResponseMapper;
import org.example.cloudstorage.model.StorageResource;
import org.example.cloudstorage.service.storage.base.AbstractStorageService;
import org.example.cloudstorage.service.storage.port.ObjectStoragePort;
import org.example.cloudstorage.service.storage.validation.PathValidator;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class FileActionService extends AbstractStorageService {

    public FileActionService(ObjectStoragePort storagePort, PathValidator pathValidator,
                             ResourceResponseMapper resourceMapper) {
        super(storagePort, pathValidator, resourceMapper);
    }

    public List<ResourceInfoDto> upload(long userId, String destinationPath, List<MultipartFile> files) throws IOException {
        log.info("Upload request: userId={}, to='{}', filesCount={}", userId, destinationPath, files.size());
        List<StorageResource> uploadedResources = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();

            pathValidator.validatePath(originalFilename, false);
            String relativePath = destinationPath + originalFilename;
            String fullPath = generateUserPath(userId, relativePath);

            log.debug("Uploading file: '{}' ({} bytes)", fullPath, file.getSize());
            pathValidator.validateUpload(fullPath);

            StorageResource resource = storagePort.uploadFile(
                    fullPath,
                    file.getInputStream(),
                    file.getSize(),
                    file.getContentType()
            );

            uploadedResources.add(resource);
        }

        return resourceMapper.toDtoList(uploadedResources);
    }

    public void delete(long userId, String path) {
        log.info("Request to delete resource: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);

        if (path.endsWith("/")) {
            log.debug("Deleting folder recursively: '{}'", fullPath);
            var pathsToDelete = storagePort.listAllPathsRecursive(fullPath);
            log.debug("Found {} objects to delete", pathsToDelete.size());
            storagePort.deleteObjects(pathsToDelete);
        } else {
            log.debug("Deleting single file: '{}'", fullPath);
            storagePort.delete(fullPath);
        }
    }

    public ResourceInfoDto move(long userId, String fromPath, String toPath) {
        log.info("Move request: userId={}, from='{}', to='{}'", userId, fromPath, toPath);
        String fullFrom = generateUserPath(userId, fromPath);
        String fullTo = generateUserPath(userId, toPath);

        log.debug("Normalized move paths: fullFrom='{}', fullTo='{}'", fullFrom, fullTo);
        pathValidator.validateMove(fullFrom, fullTo);

        if (fromPath.endsWith("/")) {
            List<String> paths = storagePort.listAllPathsRecursive(fullFrom);
            log.debug("Moving folder contents. Found {} sub-paths", paths.size());

            for (String path : paths) {
                String relativePart = path.substring(fullFrom.length());
                String newPath = fullTo + relativePart;

                log.trace("Copying: '{}' -> '{}'", path, newPath);
                storagePort.copy(path, newPath);
            }

            storagePort.deleteObjects(paths);
            log.debug("Fetching resource after move: '{}'", fullTo);
            return resourceMapper.fromDirectory(fullTo);
        } else {
            log.debug("Moving single file: '{}' -> '{}'", fullFrom, fullTo);
            storagePort.copy(fullFrom, fullTo);
            storagePort.delete(fullFrom);
            log.debug("Fetching resource after move: '{}'", fullTo);
            return resourceMapper.toDto(storagePort.getResource(fullTo));
        }
    }

    public ResourceInfoDto createFolder(long userId, String path) {
        log.info("Create folder request: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);

        pathValidator.validateCreateFolder(fullPath, generateUserPrefix(userId));
        StorageResource resource = storagePort.createFolder(fullPath);
        return resourceMapper.toDto(resource);
    }
}
