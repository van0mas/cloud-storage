package org.example.cloudstorage.unit;

import org.example.cloudstorage.exception.BadRequestException;
import org.example.cloudstorage.exception.storage.StorageConflictException;
import org.example.cloudstorage.service.storage.port.ObjectStoragePort;
import org.example.cloudstorage.service.storage.validation.PathValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathValidatorTest {

    @Mock
    private ObjectStoragePort storagePort;

    @InjectMocks
    private PathValidator pathValidator;

    @Nested
    @DisplayName("Тесты existsAnywhere")
    class ExistsAnywhereTests {
        @Test
        void shouldReturnTrue_WhenFolderExistsButFileRequested() {
            String path = "folder";
            when(storagePort.exists("folder")).thenReturn(false);
            when(storagePort.exists("folder/")).thenReturn(true); // а там папка

            assertTrue(pathValidator.existsAnywhere(path));
        }
    }

    @Test
    void shouldThrowConflict_WhenFileIsUsedAsFolder() {
        String destinationPath = "music/rock/song.mp3";
        String fileThatBlocksWay = "music/rock";

        // На любой запрос по умолчанию отвечаем false
        when(storagePort.exists(anyString())).thenReturn(false);

        // А вот для этого конкретного случая — true
        when(storagePort.exists(fileThatBlocksWay)).thenReturn(true);

        assertThrows(StorageConflictException.class, () ->
                pathValidator.validateUpload(destinationPath)
        );
    }

    @Test
    void shouldThrowException_WhenMovingFolderIntoItself() {
        String from = "my-folder/";
        String to = "my-folder/sub-folder/";

        // Сначала задаем общее поведение (всех остальных путей нет)
        when(storagePort.exists(anyString())).thenReturn(false);
        // Затем специфичное (наш 'from' существует)
        when(storagePort.exists(from)).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class,
                () -> pathValidator.validateMove(from, to));
    }

        @Test
        void shouldThrowException_WhenTypeMismatch() {
            String from = "file.txt";
            String to = "new-name/";

            when(storagePort.exists(from)).thenReturn(true);

            assertThrows(BadRequestException.class, () -> pathValidator.validateMove(from, to));
        }
    }

