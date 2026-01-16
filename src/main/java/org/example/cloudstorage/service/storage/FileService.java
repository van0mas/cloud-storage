package org.example.cloudstorage.service.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;
import org.example.cloudstorage.service.storage.action.FileActionService;
import org.example.cloudstorage.service.storage.download.FileDownloadService;
import org.example.cloudstorage.service.storage.query.FileQueryService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileService {

    private final FileActionService actionService;
    private final FileQueryService queryService;
    private final FileDownloadService downloadService;

    // Чтение
    public ResourceInfoDto getResource(long userId, String path) {
        return queryService.getResource(userId, path);
    }

    public List<ResourceInfoDto> listFolder(long userId, String path) {
        return queryService.listFolder(userId, path);
    }

    public List<ResourceInfoDto> search(long userId, String query) {
        return queryService.search(userId, query);
    }

    // Изменение
    public List<ResourceInfoDto> upload(long userId, String path, List<MultipartFile> files) throws IOException {
        return actionService.upload(userId, path, files);
    }

    public void delete(long userId, String path) {
        actionService.delete(userId, path);
    }

    public ResourceInfoDto move(long userId, String from, String to) {
        return actionService.move(userId, from, to);
    }

    public ResourceInfoDto createFolder(long userId, String path) {
        return actionService.createFolder(userId, path);
    }

    // Скачивание
    public StreamingResponseBody download(long userId, String path) {
        return downloadService.download(userId, path);
    }
}
