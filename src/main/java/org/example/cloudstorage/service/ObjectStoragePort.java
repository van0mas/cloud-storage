package org.example.cloudstorage.service;

import org.example.cloudstorage.model.StorageResource;

import java.io.InputStream;
import java.util.List;

public interface ObjectStoragePort {

    StorageResource getResource(String path);

    void delete(String path);

    void deleteObjects(List<String> paths);

    InputStream download(String path);

    StorageResource uploadFile(String fullPath, InputStream data, long size, String contentType);

    List<StorageResource> listAllObjectsRecursive(String prefix);

    List<String> listAllPathsRecursive(String prefix);

    StorageResource createFolder(String folderPath);

    List<StorageResource> listFolder(String folderPath);

    void copy(String sourcePath, String destinationPath);

    boolean exists(String path);
}
