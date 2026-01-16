package org.example.cloudstorage.controller.storage;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.annotation.user.CurrentUser;
import org.example.cloudstorage.controller.swagger.ResourceSwagger;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;
import org.example.cloudstorage.service.storage.FileService;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ResourceController implements ResourceSwagger {

    private final FileService fileService;

    @Override
    public ResourceInfoDto getResource(@CurrentUser Long userId, String path) {
        return fileService.getResource(userId, path);
    }

    @Override
    public List<ResourceInfoDto> upload(
            @CurrentUser Long userId,
            String path,
            List<MultipartFile> object) throws IOException {
        return fileService.upload(userId, path, object);
    }

    @Override
    public void deleteResource(@CurrentUser Long userId, String path) {
        fileService.delete(userId, path);
    }

    @Override
    public ResponseEntity<StreamingResponseBody> download(@CurrentUser Long userId, String path) {
        String fileName = PathUtils.getDownloadName(path);

        StreamingResponseBody responseBody = fileService.download(userId, path);

        return ResponseEntity.ok().header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @Override
    public ResourceInfoDto moveResource(@CurrentUser Long userId, String from, String to) {
        return fileService.move(userId, from, to);
    }

    @Override
    public List<ResourceInfoDto> search(@CurrentUser Long userId, String query) {
        return fileService.search(userId, query);
    }
}
