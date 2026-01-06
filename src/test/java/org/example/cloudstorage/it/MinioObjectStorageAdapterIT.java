package org.example.cloudstorage.it;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.example.cloudstorage.service.MinioObjectStorageAdapter;
import org.example.cloudstorage.exception.storage.StorageNotFoundException;
import org.example.cloudstorage.model.StorageResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Testcontainers
class MinioObjectStorageAdapterIT {

    @Container
    static GenericContainer<?> minio =
            new GenericContainer<>("minio/minio:RELEASE.2023-10-25T06-33-25Z")
                    .withEnv("MINIO_ROOT_USER", "testuser")
                    .withEnv("MINIO_ROOT_PASSWORD", "testpassword")
                    .withCommand("server /data")
                    .withExposedPorts(9000)
                    .waitingFor(
                            Wait.forHttp("/minio/health/ready").forPort(9000)
                    );

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url",
                () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.access-key", () -> "testuser");
        registry.add("minio.secret-key", () -> "testpassword");
        registry.add("minio.bucket-name", () -> "test-bucket");
    }

    @Autowired
    private MinioObjectStorageAdapter adapter;

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket-name}")
    private String bucket;

    @BeforeEach
    void setUp() throws Exception {
        if (!minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    @Test
    @DisplayName("Должен успешно загрузить и получить метаданные файла")
    void uploadAndGetResource_ShouldWork() {
        String path = "test-file.txt";
        byte[] content = "hello world".getBytes();

        adapter.uploadFile(
                path,
                new ByteArrayInputStream(content),
                content.length,
                "text/plain"
        );

        StorageResource resource = adapter.getResource(path);

        assertThat(resource.fullPath()).isEqualTo(path);
        assertThat(resource.size()).isEqualTo(content.length);
    }

    @Test
    @DisplayName("Должен выбросить StorageNotFoundException, если файла нет")
    void getResource_ShouldThrowNotFound() {
        assertThatThrownBy(() -> adapter.getResource("non-existent.txt"))
                .isInstanceOf(StorageNotFoundException.class);
    }

    @Test
    @DisplayName("Должен успешно скопировать объект")
    void copyObject_ShouldWorkCorrectly() {
        adapter.uploadFile("folder/file1.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");
        assertThat(adapter.getResource("folder/file1.txt")).isNotNull();

        adapter.copy("folder/file1.txt", "renamed-folder/file1.txt");
        assertThat(adapter.getResource("renamed-folder/file1.txt")).isNotNull();
    }

    @Test
    @DisplayName("Должен успешно удалить файл")
    void deleteFile_ShouldWorkCorrectly() {
        String path = "file-to-delete.txt";
        adapter.uploadFile(path, new ByteArrayInputStream(new byte[0]), 0, "text/plain");

        assertThat(adapter.getResource(path)).isNotNull();
        adapter.delete(path);
        assertThatThrownBy(() -> adapter.getResource(path))
                .isInstanceOf(StorageNotFoundException.class);
    }

    @Test
    @DisplayName("Должен успешно удалить папку и все её содержимое")
    void deleteFolder_ShouldWorkCorrectly() {
        adapter.createFolder("folder/");
        adapter.uploadFile("folder/file1.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");
        adapter.uploadFile("folder/sub/file2.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");

        assertThat(adapter.getResource("folder/file1.txt")).isNotNull();
        assertThat(adapter.getResource("folder/sub/file2.txt")).isNotNull();

        adapter.deleteObjects(adapter.listAllPathsRecursive("folder/"));

        assertThatThrownBy(() -> adapter.getResource("folder/file1.txt"))
                .isInstanceOf(StorageNotFoundException.class);
        assertThatThrownBy(() -> adapter.getResource("folder/sub/file2.txt"))
                .isInstanceOf(StorageNotFoundException.class);
    }

    @Test
    @DisplayName("Должен найти все пути рекурсивно в папке, включая папку")
    void listAllObjectsRecursive_ShouldReturnAllPaths() {
        adapter.createFolder("docs2/");
        adapter.uploadFile("docs2/file1.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");
        adapter.uploadFile("docs2/subdir/file2.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");

        List<String> allFiles = adapter.listAllPathsRecursive("docs2/");

        List<String> normalized = allFiles.stream()
                .map(p -> p.substring(p.indexOf("docs2/")))
                .toList();

        assertThat(normalized)
                .containsExactlyInAnyOrder(
                        "docs2/",
                        "docs2/file1.txt",
                        "docs2/subdir/file2.txt"
                );
    }

    @Test
    @DisplayName("Должен найти все объекты рекурсивно в папке, включая папку")
    void search_ShouldFindFilesRecursively() {
        adapter.createFolder("docs/");
        adapter.uploadFile("docs/test1.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");
        adapter.uploadFile("docs/sub/test2.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");
        adapter.uploadFile("other.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");

        List<StorageResource> found = adapter.listAllObjectsRecursive("docs/");

        assertThat(found)
                .extracting(StorageResource::fullPath)
                .containsExactlyInAnyOrder(
                            "docs/",
                        "docs/test1.txt",
                        "docs/sub/test2.txt"
                );
    }

    @Test
    @DisplayName("Должен успешно скачать содержимое файла")
    void download_ShouldReturnCorrectContent() throws Exception {
        String path = "download-test.txt";
        byte[] content = "test content".getBytes();
        adapter.uploadFile(path, new ByteArrayInputStream(content), content.length, "text/plain");

        try (InputStream downloaded = adapter.download(path)) {
            assertThat(downloaded.readAllBytes()).isEqualTo(content);
        }
    }

    @Test
    @DisplayName("Должен успешно создать пустую папку")
    void createFolder_ShouldCreateObjectWithSlash() {
        String folderPath = "new-folder/";
        adapter.createFolder(folderPath);

        StorageResource resource = adapter.getResource(folderPath);
        assertThat(resource.fullPath()).isEqualTo(folderPath);
        assertThat(resource.size()).isEqualTo(0);
    }

    @Test
    @DisplayName("getResource должен отличать папку от файла")
    void getResource_ShouldDistinguishFolderAndFile() {
        String filePath = "folder-to-distinguish/file.txt";
        adapter.uploadFile(filePath, new ByteArrayInputStream(new byte[0]), 0, "text/plain");

        assertThat(adapter.getResource(filePath)).isNotNull();

        assertThatThrownBy(() -> adapter.getResource(filePath + "/"))
                .isInstanceOf(StorageNotFoundException.class);
    }

    @Test
    @DisplayName("listFolder должен возвращать только объекты первого уровня")
    void listFolder_ShouldReturnNonRecursiveItems() {
        adapter.uploadFile("a/1.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");
        adapter.uploadFile("a/sub/2.txt", new ByteArrayInputStream(new byte[0]), 0, "text/plain");

        List<StorageResource> items = adapter.listFolder("a/");

        assertThat(items).hasSize(2);
    }
}
