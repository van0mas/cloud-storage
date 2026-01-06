package org.example.cloudstorage.service;

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
            return;
        }

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

            // Согласно документации, нужно обязательно проитерировать результат,
            // иначе удаление не выполнится (ленивая загрузка)
            for (Result<DeleteError> result : results) {
                try {
                    DeleteError error = result.get();
                    log.error("Error deleting object {}: {}", error.objectName(), error.message());
                } catch (Exception e) {
                    log.error("Error while reading delete result", e);
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
            return result;
        });
    }

    @Override
    public List<StorageResource> listAllObjectsRecursive(String prefix) {
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
            return  true;
        } catch (StorageNotFoundException e) {
            return false;
        }
    }

    private <T> T handleRequest(String path, CheckedSupplier<T> action) {
        try {
            return action.get();
        } catch (ErrorResponseException e) {
            switch (e.errorResponse().code()) {
                case "NoSuchKey", "NoSuchBucket" ->
                        throw new StorageNotFoundException(path, e);

                case "AccessDenied" ->
                        throw new StorageAccessDeniedException(path, e);

                case "BucketAlreadyExists", "ObjectAlreadyExists" ->
                        throw new StorageConflictException(path);

                default ->
                        throw new StorageInternalException(e);
            }

        } catch (ServerException | IOException e) {
            throw new StorageUnavailableException(e);

        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new StorageInternalException(e);

        } catch (Exception e) {
            throw new StorageInternalException(e);
        }
    }

    @FunctionalInterface
    private interface CheckedSupplier<T> {
        T get() throws Exception;
    }
}
