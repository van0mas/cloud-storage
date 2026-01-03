package org.example.cloudstorage.service;

import org.example.cloudstorage.model.StorageResource;

import java.io.InputStream;
import java.util.List;

public interface ObjectStoragePort {

    StorageResource getResource(String path);

    void delete(String path);

    InputStream download(String path);

    StorageResource renameOrMove(String fromPath, String toPath);

    List<StorageResource> search(String prefix, String query);

    StorageResource uploadFile(String fullPath, InputStream data, long size, String contentType);

    List<StorageResource> listFolder(String folderPath);

    StorageResource createFolder(String folderPath);
}
