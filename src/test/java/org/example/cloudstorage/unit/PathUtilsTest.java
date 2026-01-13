package org.example.cloudstorage.unit;

import org.example.cloudstorage.util.PathUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PathUtilsTest {

    @Test
    @DisplayName("Нормализация путей: очистка слэшей и пробелов")
    void normalize_ShouldCleanPaths() {
        assertAll(
                () -> assertEquals("folder/file.txt", PathUtils.normalize("  ///folder//file.txt ")),
                () -> assertEquals("folder/sub/", PathUtils.normalize("folder\\sub\\")),
                () -> assertEquals("", PathUtils.normalize("/")),
                () -> assertEquals("a/b/c/", PathUtils.normalize("a/b/c/"))
        );
    }

    @Test
    @DisplayName("Извлечение родительского пути")
    void extractParentPath_ShouldReturnPathWithTrailingSlash() {
        assertAll(
                () -> assertEquals("user-1/docs/", PathUtils.extractParentPath("user-1/docs/file.txt")),
                () -> assertEquals("user-1/", PathUtils.extractParentPath("user-1/docs/")),
                () -> assertEquals("", PathUtils.extractParentPath("file.txt"))
        );
    }

    @Test
    @DisplayName("Генерация цепочки родителей для проверки препятствий")
    void getParentSteps_ShouldReturnAllParentFolders() {
        List<String> steps = PathUtils.getParentSteps("a/b/c/file.txt");
        // Ожидаем: "a/", "a/b/", "a/b/c/"
        assertEquals(List.of("a/", "a/b/", "a/b/c/"), steps);
    }
}
