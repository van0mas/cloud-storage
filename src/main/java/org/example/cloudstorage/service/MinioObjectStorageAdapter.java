package org.example.cloudstorage.service;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.ServerException;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
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
    public List<StorageResource> search(String prefix, String query) {
        return handleRequest(prefix, () -> {
            List<StorageResource> result = new ArrayList<>();

            Iterable<Result<Item>> items = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucket)
                            .prefix(prefix)
                            .recursive(true)
                            .build()
            );

            for (Result<Item> r : items) {
                Item item = r.get();
                if (item.objectName().contains(query)) {
                    result.add(new StorageResource(item.objectName(), item.size()));
                }
            }
            return result;
        });
    }

    @Override
    public StorageResource renameOrMove(String fromPath, String toPath) {
        StorageResource source = getResource(fromPath);

        if (exists(toPath)) {
            throw new StorageConflictException(toPath);
        }

        handleRequest(toPath, () ->
                minioClient.copyObject(
                        CopyObjectArgs.builder()
                                .bucket(bucket)
                                .object(toPath)
                                .source(CopySource.builder()
                                                .bucket(bucket)
                                                .object(fromPath)
                                                .build())
                                .build()
                )
        );

        delete(fromPath);
        return new StorageResource(toPath, source.size());
    }

    private boolean exists(String path) {
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
