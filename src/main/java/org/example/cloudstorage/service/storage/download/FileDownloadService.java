package org.example.cloudstorage.service.storage.download;

import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.storage.StorageNotFoundException;
import org.example.cloudstorage.mapper.ResourceResponseMapper;
import org.example.cloudstorage.service.storage.port.ObjectStoragePort;
import org.example.cloudstorage.service.storage.validation.PathValidator;
import org.example.cloudstorage.service.storage.base.AbstractStorageService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class FileDownloadService extends AbstractStorageService {

    public FileDownloadService(ObjectStoragePort storagePort, PathValidator pathValidator,
                               ResourceResponseMapper resourceMapper) {
        super(storagePort, pathValidator, resourceMapper);
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
}
