package org.example.cloudstorage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        log.info("Connecting to MinIO at: {}", minioProperties.getUrl());

        MinioClient client = MinioClient.builder()
                .endpoint(minioProperties.getUrl())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();

        initializeBucket(client);

        return client;
    }

    private void initializeBucket(MinioClient client) {
        String bucket = minioProperties.getBucketName();
        try {
            log.debug("Checking if bucket '{}' exists...", bucket);
            boolean found = client.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );

            if (!found) {
                log.info("Bucket '{}' not found. Creating it now...", bucket);
                client.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
                log.info("Bucket '{}' created successfully.", bucket);
            } else {
                log.debug("Bucket '{}' already exists. Skipping creation.", bucket);
            }
        } catch (Exception e) {
            log.error("Critical error initializing MinIO bucket '{}': {}", bucket, e.getMessage());
            throw new RuntimeException("Could not initialize MinIO storage", e);
        }
    }
}
