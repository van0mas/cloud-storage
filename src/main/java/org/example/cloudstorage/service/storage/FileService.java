package org.example.cloudstorage.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.config.AppConstants;
import org.example.cloudstorage.dto.ResourceInfoDto;
import org.example.cloudstorage.exception.storage.StorageNotFoundException;
import org.example.cloudstorage.mapper.ResourceResponseMapper;
import org.example.cloudstorage.model.StorageResource;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final ObjectStoragePort storagePort;
    private final ResourceResponseMapper resourceMapper;
    private final PathValidator pathValidator;

    public ResourceInfoDto getResource(long userId, String path) {
        log.info("Request to get resource: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);
        log.debug("Full path for getResource: '{}'", fullPath);
        return resourceMapper.toDto(storagePort.getResource(fullPath));
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

    public StreamingResponseBody download(long userId, String path) {
        log.info("Request to download: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);

        if (!path.endsWith("/")) {
            log.debug("Direct file download: '{}'", fullPath);
            return outputStream -> {
                try (InputStream is = storagePort.download(fullPath)) {
                    is.transferTo(outputStream);
                }
            };
        }

        log.debug("Folder download (zip) started: '{}'", fullPath);
        List<String> allPaths = storagePort.listAllPathsRecursive(fullPath);
        if (allPaths.isEmpty()) {
            log.warn("Folder is empty or not found for download: '{}'", fullPath);
            throw new StorageNotFoundException(path);
        }

        return outputStream -> {
            try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
                for (String objectPath : allPaths) {
                    String zipEntryName = objectPath.substring(fullPath.length());
                    if (zipEntryName.isEmpty()) continue;

                    zos.putNextEntry(new ZipEntry(zipEntryName));

                    if (!objectPath.endsWith("/")) {
                        try (InputStream is = storagePort.download(objectPath)) {
                            is.transferTo(zos);
                        }
                    }
                    zos.closeEntry();
                }
                zos.finish();
                zos.flush();
            }
        };
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

    public List<ResourceInfoDto> listFolder(long userId, String path) {
        log.info("List folder request: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);
        var resources = storagePort.listFolder(fullPath);

        return resourceMapper.toDtoList(resources.stream()
                .filter(resource -> !resource.fullPath().equals(fullPath)).toList());
    }

    public ResourceInfoDto createFolder(long userId, String path) {
        log.info("Create folder request: userId={}, path='{}'", userId, path);
        String fullPath = generateUserPath(userId, path);

        pathValidator.validateCreateFolder(fullPath, generateUserPrefix(userId));
        StorageResource resource = storagePort.createFolder(fullPath);
        return resourceMapper.toDto(resource);
    }

    private String generateUserPath(long userId, String path) {
        return generateUserPrefix(userId) + PathUtils.normalize(path);
    }

    private String generateUserPrefix(long userId) {
        return AppConstants.Storage.USER_ROOT_TEMPLATE.formatted(userId);
    }
}
