package org.example.cloudstorage.service.storage;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.config.AppConstants;
import org.example.cloudstorage.exception.storage.*;
import org.example.cloudstorage.exception.BadRequestException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.*;

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
                throw new BadRequestException(FOLDER_PARENT_MISSING);
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
            throw new BadRequestException(MOVE_TYPE_MISMATCH);
        }

        if (to.startsWith(from)) {
            throw new BadRequestException(MOVE_INTO_ITSELF);
        }
    }

    public void validatePath(String path, boolean mustBeDirectory) throws BadRequestException {
        if (path == null || path.isBlank()) return;

        if (path.length() > AppConstants.Storage.MAX_PATH_LENGTH) {
            throw new BadRequestException(PATH_TOO_LONG);
        }

        if (path.contains("..")) {
            throw new BadRequestException(PATH_CONTAINS_DOTS);
        }

        if (!AppConstants.Storage.PATH_PATTERN.matcher(path).matches()) {
            throw new BadRequestException(PATH_INVALID_CHARACTERS);
        }

        if (mustBeDirectory && !path.endsWith("/")) {
            throw new BadRequestException(PATH_MUST_BE_DIRECTORY);
        }
    }
}
