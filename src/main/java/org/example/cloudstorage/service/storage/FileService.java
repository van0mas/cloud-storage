package org.example.cloudstorage.service.storage;

import lombok.RequiredArgsConstructor;
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

@Service
@RequiredArgsConstructor
public class FileService {

    private final ObjectStoragePort storagePort;
    private final ResourceResponseMapper resourceMapper;
    private final PathValidator pathValidator;

    public ResourceInfoDto getResource(long userId, String path) {
        return resourceMapper.toDto(storagePort.getResource(generateUserPath(userId, path)));
    }

    public void delete(long userId, String path) {
        String fullPath = generateUserPath(userId, path);

        if (path.endsWith("/")) {
            var pathsToDelete = storagePort.listAllPathsRecursive(fullPath);
            storagePort.deleteObjects(pathsToDelete);
        } else {
            storagePort.delete(fullPath);
        }
    }

    public StreamingResponseBody download(long userId, String path) {
        String fullPath = generateUserPath(userId, path);

        if (!path.endsWith("/")) {
            return outputStream -> {
                try (InputStream is = storagePort.download(fullPath)) {
                    is.transferTo(outputStream);
                }
            };
        }

        List<String> allPaths = storagePort.listAllPathsRecursive(fullPath);
        if (allPaths.isEmpty()) {
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
        var foundResources = storagePort.listAllObjectsRecursive(generateUserPrefix(userId));

        var result = foundResources.stream()
                .filter(res -> res.fullPath().toLowerCase().contains(query.toLowerCase()))
                .toList();
        return resourceMapper.toDtoList(result);
    }

    public List<ResourceInfoDto> upload(long userId, String destinationPath, List<MultipartFile> files) throws IOException {
        List<StorageResource> uploadedResources = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFilename = file.getOriginalFilename();

            pathValidator.validatePath(originalFilename, false);
            String relativePath = destinationPath + originalFilename;
            String fullPath = generateUserPath(userId, relativePath);

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

    public void move(long userId, String fromPath, String toPath) {
        String fullFrom = generateUserPath(userId, fromPath);
        String fullTo = generateUserPath(userId, toPath);

        pathValidator.validateMove(fullFrom, fullTo);

        if (fromPath.endsWith("/")) {
            List<String> paths = storagePort.listAllPathsRecursive(fullFrom);

            for (String path : paths) {
                String relativePart = path.substring(fullFrom.length());
                String newPath = fullTo + relativePart;

                storagePort.copy(path, newPath);
            }

            storagePort.deleteObjects(paths);

        } else {
            storagePort.copy(fullFrom, fullTo);
            storagePort.delete(fullFrom);
        }
    }

    public List<ResourceInfoDto> listFolder(long userId, String path) {
        String fullPath = generateUserPath(userId, path);
        var resources = storagePort.listFolder(fullPath);
        return resourceMapper.toDtoList(resources.stream()
                .filter(resource -> !resource.fullPath().equals(fullPath)).toList());
    }

    public ResourceInfoDto createFolder(long userId, String path) {
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
