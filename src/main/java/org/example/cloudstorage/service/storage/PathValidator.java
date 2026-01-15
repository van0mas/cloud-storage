package org.example.cloudstorage.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.config.AppConstants;
import org.example.cloudstorage.exception.storage.*;
import org.example.cloudstorage.exception.BadRequestException;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class PathValidator {

    private final ObjectStoragePort storagePort;

    public boolean existsAnywhere(String path) {
        log.trace("Validator: checking existence for '{}'", path);

        if (storagePort.exists(path)) {
            log.trace("Validator: found exact match for '{}'", path);
            return true;
        }

        String alternative = path.endsWith("/")
                ? PathUtils.trimTrailingSlash(path)
                : path + "/";

        if (storagePort.exists(alternative)) {
            log.trace("Validator: found alternative match for '{}' as '{}'", path, alternative);
            return true;
        }

        if (path.endsWith("/")) {
            boolean hasChildren = !storagePort.listAllPathsRecursive(path).isEmpty();
            if (hasChildren) {
                log.trace("Validator: found virtual directory (has children) for '{}'", path);
            }
            return hasChildren;
        }

        return false;
    }

    public void validateCreateFolder(String path, String userPrefix) {
        log.debug("Validator: validating folder creation for path='{}', userPrefix='{}'", path, userPrefix);
        if (existsAnywhere(path)) {
            log.warn("Validator: folder creation failed - path already exists: '{}'", path);
            throw new StorageConflictException(path);
        }

        String parentPath = PathUtils.extractParentPath(path);
        if (!parentPath.equals(userPrefix)) {
            if (!existsAnywhere(parentPath)) {
                log.warn("Validator: folder creation failed - parent missing: '{}'", parentPath);
                throw new BadRequestException(FOLDER_PARENT_MISSING);
            }
        }
    }

    public void validateUpload(String path) {
        log.debug("Validator: validating upload for path='{}'", path);
        if (existsAnywhere(path)) {
            log.warn("Validator: upload failed - path already exists: '{}'", path);
            throw new StorageConflictException(path);
        }

        List<String> steps = PathUtils.getParentSteps(path);
        for (String step : steps) {
            String stepAsFile = PathUtils.trimTrailingSlash(step);
            if (storagePort.exists(stepAsFile)) {
                log.warn("Validator: upload failed - parent step '{}' is a file, cannot be a directory", stepAsFile);
                throw new StorageConflictException(stepAsFile);
            }
        }
    }

    public void validateMove(String from, String to) {
        log.info("Validator: validating move from='{}' to='{}'", from, to);

        if (!existsAnywhere(from)) {
            log.warn("Validator: move failed - source does not exist: '{}'", from);
            throw new StorageNotFoundException(from);
        }

        if (existsAnywhere(to)) {
            log.warn("Validator: move failed - destination already exists: '{}'", to);
            throw new StorageConflictException(to);
        }

        if (from.endsWith("/") != to.endsWith("/")) {
            log.warn("Validator: move failed - type mismatch (file vs directory)");
            throw new BadRequestException(MOVE_TYPE_MISMATCH);
        }

        if (to.startsWith(from)) {
            log.warn("Validator: move failed - attempt to move into itself");
            throw new BadRequestException(MOVE_INTO_ITSELF);
        }
    }

    public void validatePath(String path, boolean mustBeDirectory) throws BadRequestException {
        log.trace("Validator: checking path constraints for '{}', mustBeDir={}", path, mustBeDirectory);

        if (path == null || path.isBlank()) return;

        if (path.length() > AppConstants.Storage.MAX_PATH_LENGTH) {
            log.warn("Validator: path too long ({} chars)", path.length());
            throw new BadRequestException(PATH_TOO_LONG);
        }

        if (path.contains("..")) {
            log.warn("Validator: path contains forbidden dots: '{}'", path);
            throw new BadRequestException(PATH_CONTAINS_DOTS);
        }

        if (!AppConstants.Storage.PATH_PATTERN.matcher(path).matches()) {
            log.warn("Validator: path contains invalid characters: '{}'", path);
            throw new BadRequestException(PATH_INVALID_CHARACTERS);
        }

        if (mustBeDirectory && !path.endsWith("/")) {
            log.warn("Validator: path must be a directory but doesn't end with '/': '{}'", path);
            throw new BadRequestException(PATH_MUST_BE_DIRECTORY);
        }
    }
}
