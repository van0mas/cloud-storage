package org.example.cloudstorage.service.storage;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.ServerException;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.exception.storage.*;
import org.example.cloudstorage.model.StorageResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class MinioObjectStorageAdapter implements ObjectStoragePort {

    private final MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucket;

    @Override
    public StorageResource getResource(String path) {
        return handleRequest(path, () -> {
            log.trace("MinIO: Stat object request: '{}'", path);
            var stat = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );

            return new StorageResource(path, stat.size());
        });
    }

    @Override
    public void delete(String path) {
        handleRequest(path, () -> {
            log.debug("MinIO: Remove object request: '{}'", path);
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(path)
                            .build()
            );
            return null;
        });
    }

    @Override
    public void deleteObjects(List<String> paths) {
        if (paths == null || paths.isEmpty()) {
            log.debug("MinIO: Batch delete requested with empty list");
            return;
        }

        log.debug("MinIO: Batch delete started for {} objects", paths.size());
        List<DeleteObject> objects = paths.stream()
                .map(DeleteObject::new)
                .toList();

        handleRequest("Batch delete", () -> {
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(
                    RemoveObjectsArgs.builder()
                            .bucket(bucket)
                            .objects(objects)
                            .build()
            );

            for (Result<DeleteError> result : results) {
                try {
                    DeleteError error = result.get();
                    log.error("MinIO: Error deleting object {}: {}", error.objectName(), error.message());
                } catch (Exception e) {
                    log.error("MinIO: Error while reading delete result", e);
                }
            }
            return null;
        });
    }

    @Override
    public StorageResource uploadFile(
            String path,
            InputStream data,
            long size,
            String contentType
    ) {
        log.debug("MinIO: Uploading object: '{}' ({} bytes, {})", path, size, contentType);
        handleRequest(path, () ->
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(path)
                                .stream(data, size, -1)
                                .contentType(contentType)
                                .build()
                )
        );

        return new StorageResource(path, size);
    }

    @Override
    public InputStream download(String path) {
        log.debug("MinIO: Download object request: '{}'", path);
        return handleRequest(path, () ->
                minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(bucket)
                                .object(path)
                                .build()
                )
        );
    }

    @Override
    public StorageResource createFolder(String path) {
        log.debug("MinIO: Creating folder marker: '{}'", path);
        handleRequest(path, () ->
                minioClient.putObject(
                        PutObjectArgs.builder()
                                .bucket(bucket)
                                .object(path)
                                .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                                .build()
                )
        );

        return new StorageResource(path, 0);
    }

    @Override
    public void copy(String sourcePath, String destinationPath) {
        log.debug("MinIO: Copy object: '{}' -> '{}'", sourcePath, destinationPath);
        handleRequest(sourcePath, () -> {
            minioClient.copyObject(
                    CopyObjectArgs.builder()
                            .bucket(bucket)
                            .object(destinationPath)
                            .source(CopySource.builder().bucket(bucket).object(sourcePath).build())
                            .build()
            );
            return null;
        });
    }

    @Override
    public List<StorageResource> listFolder(String path) {
        log.debug("MinIO: List objects (flat) with prefix: '{}'", path);
        return handleRequest(path, () -> {
            List<StorageResource> result = new ArrayList<>();

            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(path)
                            .delimiter("/")
                            .build()
            );

            for (Result<Item> r : items) {
                Item item = r.get();
                result.add(new StorageResource(item.objectName(), item.size()));
            }
            log.trace("MinIO: Found {} items in prefix '{}'", result.size(), path);
            return result;
        });
    }

    @Override
    public List<StorageResource> listAllObjectsRecursive(String prefix) {
        log.debug("MinIO: List objects (recursive) with prefix: '{}'", prefix);
        return handleRequest(prefix, () -> {
            List<StorageResource> resources = new ArrayList<>();

            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> r : items) {
                Item item = r.get();
                resources.add(new StorageResource(item.objectName(), item.size()));
            }
            log.trace("MinIO: Found {} items recursively in prefix '{}'", resources.size(), prefix);
            return resources;
        });
    }

    @Override
    public List<String> listAllPathsRecursive(String prefix) {
        return listAllObjectsRecursive(prefix).stream()
                .map(StorageResource::fullPath)
                .toList();
    }

    public boolean exists(String path) {
        try {
            getResource(path);
            return true;
        } catch (StorageNotFoundException e) {
            return false;
        }
    }

    private <T> T handleRequest(String path, CheckedSupplier<T> action) {
        try {
            return action.get();
        } catch (ErrorResponseException e) {
            String code = e.errorResponse().code();
            log.warn("MinIO: Error response for path '{}': code={}, message={}",
                    path, code, e.errorResponse().message());

            switch (code) {
                case "NoSuchKey", "NoSuchBucket" ->
                        throw new StorageNotFoundException(path, e);

                case "AccessDenied" ->
                        throw new StorageAccessDeniedException(path, e);

                case "BucketAlreadyExists", "ObjectAlreadyExists" ->
                        throw new StorageConflictException(path);

                default ->
                        throw new StorageInternalException(path, e);
            }

        } catch (ServerException | IOException e) {
            log.error("MinIO: Connection or server error for path '{}': {}", path, e.getMessage());
            throw new StorageUnavailableException(path, e);

        } catch (Exception e) {
            log.error("MinIO: Unexpected error for path '{}': {}", path, e.getMessage(), e);
            throw new StorageInternalException(path, e);
        }
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
