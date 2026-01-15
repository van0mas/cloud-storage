package org.example.cloudstorage.service.storage;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.exception.storage.StorageConflictException;
import org.example.cloudstorage.exception.storage.StorageNotFoundException;
import org.example.cloudstorage.exception.BadRequestException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PathValidator {

    private final ObjectStoragePort storagePort;

    public boolean existsAnywhere(String path) {

        if (storagePort.exists(path)) {
            return true;
        }

        String alternative = path.endsWith("/")
                ? PathUtils.trimTrailingSlash(path)
                : path + "/";

        if (storagePort.exists(alternative)) {
            return true;
        }

        if (path.endsWith("/")) {
            return !storagePort.listAllObjectsRecursive(path).isEmpty();
        }

        return false;
    }

    public void validateCreateFolder(String path, String userPrefix) {
        if (existsAnywhere(path)) {
            throw new StorageConflictException(path);
        }

        String parentPath = PathUtils.extractParentPath(path);
        if (!parentPath.equals(userPrefix)) {
            if (!existsAnywhere(parentPath)) {
                throw new BadRequestException("Родительская папка не существует. Вручную нельзя создавать вложенные папки.");
            }
        }
    }

    public void validateUpload(String path) {
        if (existsAnywhere(path)) {
            throw new StorageConflictException(path);
        }

        List<String> steps = PathUtils.getParentSteps(path);
        for (String step : steps) {
            String stepAsFile = PathUtils.trimTrailingSlash(step);
            if (storagePort.exists(stepAsFile)) {
                throw new StorageConflictException(stepAsFile);
            }
        }
    }

    public void validateMove(String from, String to) {
        if (!existsAnywhere(from)) {
            throw new StorageNotFoundException(from);
        }

        if (existsAnywhere(to)) {
            throw new StorageConflictException(to);
        }

        if (from.endsWith("/") != to.endsWith("/")) {
            throw new BadRequestException("Нельзя менять тип ресурса (файл/папка) при перемещении");
        }

        if (to.startsWith(from)) {
            throw new BadRequestException("Нельзя переместить папку в саму себя");
        }
    }

    public void validatePath(String path, boolean mustBeDirectory) throws BadRequestException {
        if (path == null || path.isBlank()) return;

        if (path.contains("..")) {
            throw new BadRequestException("Путь не может содержать '..'");
        }

        if (!path.matches("^[a-zA-Zа-яА-ЯёЁ0-9 /_.-]+$")) {
            throw new BadRequestException("Путь содержит недопустимые символы");
        }

        if (mustBeDirectory && !path.endsWith("/")) {
            throw new BadRequestException("Путь к директории должен заканчиваться на '/'");
        }
    }
}
