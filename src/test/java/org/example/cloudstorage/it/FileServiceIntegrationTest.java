package org.example.cloudstorage.it;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.assertj.core.api.Assertions;
import org.example.cloudstorage.dto.ResourceInfoDto;
import org.example.cloudstorage.exception.storage.StorageConflictException;
import org.example.cloudstorage.exception.storage.StorageNotFoundException;
import org.example.cloudstorage.service.storage.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("dev")
class FileServiceIntegrationTest {

    @Container
    static GenericContainer<?> minio = new GenericContainer<>("minio/minio:RELEASE.2023-10-25T06-33-25Z")
            .withEnv("MINIO_ROOT_USER", "testuser")
            .withEnv("MINIO_ROOT_PASSWORD", "testpassword")
            .withCommand("server /data")
            .withExposedPorts(9000)
            .waitingFor(Wait.forHttp("/minio/health/ready").forPort(9000));

    @DynamicPropertySource
    static void minioProperties(DynamicPropertyRegistry registry) {
        registry.add("minio.url", () -> "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
        registry.add("minio.access-key", () -> "testuser");
        registry.add("minio.secret-key", () -> "testpassword");
        registry.add("minio.bucket-name", () -> "test-bucket");
    }

    @Autowired
    private FileService fileService;

    @Autowired
    private MinioClient minioClient;

    private final long userId = 1L;

    @BeforeEach
    void setUp() throws Exception {
        String bucket = "test-bucket";
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }

    @Test
    @DisplayName("Поиск с пустой строкой должен возвращать все файлы пользователя")
    void search_EmptyQuery_ShouldReturnAllFiles() throws IOException {
        long uniqueUserId = 999L;

        fileService.upload(uniqueUserId, "dir1/", List.of(
                new MockMultipartFile("files", "1.txt", "text/plain", "1".getBytes())
        ));
        fileService.upload(uniqueUserId, "dir2/", List.of(
                new MockMultipartFile("files", "2.txt", "text/plain", "2".getBytes())
        ));

        var results = fileService.search(uniqueUserId, "");

        assertEquals(2, results.size());
    }

    @Test
    @DisplayName("Перемещение папки должно обновлять пути всех вложенных файлов")
    void move_Folder_ShouldRelocateAllNestedFiles() throws IOException {
        String content = "Hello Integration Test";
        MockMultipartFile file = new MockMultipartFile("files", "sub/test.txt",
                "text/plain", content.getBytes());
        fileService.upload(userId, "docs/", List.of(file));

        // Act: Перемещаем папку docs/ в archive/
        fileService.move(userId, "docs/", "archive/");

        // Assert: По новому пути файл есть
        var newResource = fileService.getResource(userId, "archive/sub/test.txt");
        assertEquals("test.txt", newResource.name());

        // По старому пути — 404
        assertThrows(StorageNotFoundException.class, () ->
                fileService.getResource(userId, "docs/sub/test.txt")
        );
    }

    @Test
    @DisplayName("Скачивание папки должно возвращать валидный ZIP архив")
    void download_Folder_ShouldReturnValidZip() throws IOException {
        fileService.upload(userId, "images/", List.of(
                new MockMultipartFile("files", "pic1.jpg", "image/jpeg", "data1".getBytes()),
                new MockMultipartFile("files", "pic2.jpg", "image/jpeg", "data2".getBytes())
        ));

        Object result = fileService.download(userId, "images/");
        assertTrue(result instanceof StreamingResponseBody, "Должен вернуться StreamingResponseBody для папки");

        StreamingResponseBody srb = (StreamingResponseBody) result;

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        srb.writeTo(out);

        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(out.toByteArray()))) {
            int count = 0;
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                count++;
                String name = entry.getName();
                assertTrue(name.contains("pic1.jpg") || name.contains("pic2.jpg"),
                        "Неверное имя файла в архиве: " + name);
                zis.closeEntry();
            }
            assertEquals(2, count, "Количество файлов в архиве не совпадает");
        }
    }

    @Test
    @DisplayName("Поиск должен быть регистронезависимым и находить вхождения")
    void search_ShouldBeCaseInsensitive() throws IOException {
        fileService.upload(userId, "work/", List.of(
                new MockMultipartFile("files", "REPORT_2024.pdf", "application/pdf", "data".getBytes())
        ));

        var results = fileService.search(userId, "report");

        assertEquals(1, results.size());
        assertTrue(results.get(0).name().contains("REPORT_2024"));
    }

    @Test
    @DisplayName("Загрузка должна чистить грязные пути через нормализацию")
    void upload_ShouldNormalizeDirtyPaths() throws IOException {
        fileService.upload(userId, "///my//dirty/path///", List.of(
                new MockMultipartFile("files", "clean.txt", "text/plain", "data".getBytes())
        ));

        var resource = fileService.getResource(userId, "my/dirty/path/clean.txt");
        assertNotNull(resource);
    }

    @Test
    @DisplayName("Валидация должна запрещать загрузку, если на пути стоит файл-преграда")
    void upload_ShouldFail_WhenFileBlockerExists() throws IOException {
        fileService.upload(userId, "", List.of(
                new MockMultipartFile("files", "blocker.txt", "text/plain", "data".getBytes())
        ));

        MockMultipartFile subFile = new MockMultipartFile("files", "any.txt", "text/plain", "data".getBytes());

        assertThrows(StorageConflictException.class, () ->
                fileService.upload(userId, "blocker.txt/", List.of(subFile))
        );
    }

    @Test
    @DisplayName("Изоляция: Пользователь не должен видеть файлы другого пользователя")
    void isolation_ShouldPreventCrossUserAccess() throws IOException {
        long user2 = 2L;
        // User 1 грузит файл
        fileService.upload(userId, "", List.of(
                new MockMultipartFile("files", "private.txt", "text/plain", "u1".getBytes())));

        // User 2 ищет его
        var results = fileService.search(user2, "private");

        // Assert
        assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Нельзя создать папку с именем, которое уже занято файлом")
    void createFolder_ShouldFail_WhenFileNameAlreadyExists() throws IOException {
        fileService.upload(userId, "", List.of(
                new MockMultipartFile("files", "document", "text/plain", "data".getBytes())
        ));

        assertThrows(StorageConflictException.class, () ->
                fileService.createFolder(userId, "document/")
        );
    }

    @Test
    @DisplayName("Запрет перемещения папки в саму себя (вглубь структуры)")
    void move_ShouldFail_WhenMovingIntoNestedSelf() {
        fileService.createFolder(userId, "projects/");

        assertThrows(IllegalArgumentException.class, () ->
                fileService.move(userId, "projects/", "projects/java/")
        );
    }

    @Test
    @DisplayName("Корректная работа с кириллицей и пробелами")
    void upload_ShouldHandleRussianCharactersAndSpaces() throws IOException {
        String folderName = "Мои Документы/";
        String fileName = "Отчёт за 2026 год.pdf";

        fileService.upload(userId, folderName, List.of(
                new MockMultipartFile("files", fileName, "application/pdf", "data".getBytes())
        ));

        var results = fileService.search(userId, "Отчёт");

        ResourceInfoDto found = results.get(0);

        assertThat(found.name()).isEqualTo("Отчёт за 2026 год.pdf");

        assertThat(found.path()).contains("Мои Документы");
    }

    @Test
    @DisplayName("Удаление папки должно рекурсивно удалять всё содержимое")
    void delete_Folder_ShouldRemoveAllNestedContent() throws IOException {
        fileService.upload(userId, "a/b/", List.of(
                new MockMultipartFile("files", "c.txt", "text/plain", "data".getBytes())
        ));

        var res = fileService.search(userId, "c.txt");
        assertTrue(res.size() == 1, "Файл c.txt должен существовать перед удалением");
        fileService.delete(userId, "a/");

        var results = fileService.search(userId, "c.txt");
        assertTrue(results.isEmpty(), "Хранилище должно быть пустым после удаления корневой папки");
    }

    @Test
    @DisplayName("listFolder должен возвращать объекты первого уровня")
    void listFolder_ShouldReturnNonRecursiveItems() throws IOException {
        fileService.createFolder(userId, "a/");
        fileService.upload(userId, "a/a/", List.of(
                new MockMultipartFile("files", "clean.txt", "text/plain", "data".getBytes())
        ));
        fileService.upload(userId, "a/b/b/", List.of(
                new MockMultipartFile("files", "clean.txt", "text/plain", "data".getBytes())
        ));

        fileService.upload(userId, "a/c/b/", List.of(
                new MockMultipartFile("files", "clean.txt", "text/plain", "data".getBytes())
        ));


        List<ResourceInfoDto> items = fileService.listFolder(userId,"a/");

        Assertions.assertThat(items).hasSize(3);
    }
}
