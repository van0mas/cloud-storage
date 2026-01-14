
package org.example.cloudstorage.controller;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.annotation.CurrentUser;
import org.example.cloudstorage.annotation.ValidPath;
import org.example.cloudstorage.dto.ResourceInfoDto;
import org.example.cloudstorage.service.storage.FileService;
import org.example.cloudstorage.util.PathUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/")
@RequiredArgsConstructor
@Validated
public class FileController {

    private final FileService fileService;

    @GetMapping("/resource")
    public ResourceInfoDto getResource(@CurrentUser Long userId,
                                       @RequestParam @ValidPath @NotBlank String path) {
        return fileService.getResource(userId, path);
    }

    @DeleteMapping("/resource")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@CurrentUser Long userId,
                               @RequestParam @ValidPath @NotBlank String path) {
        fileService.delete(userId, path);
    }

    @GetMapping("/resource/download")
    public ResponseEntity<StreamingResponseBody> download(@CurrentUser Long userId,
                                                          @RequestParam @ValidPath @NotBlank String path) {

        String fileName = PathUtils.getDownloadName(path);

        StreamingResponseBody responseBody = fileService.download(userId, path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @GetMapping("/resource/move")
    public ResourceInfoDto moveResource(@CurrentUser Long userId,
                                        @RequestParam @ValidPath @NotBlank String from,
                                        @RequestParam @ValidPath @NotBlank String to) {
        return fileService.move(userId, from, to);
    }

    @GetMapping("/resource/search")
    public List<ResourceInfoDto> search(@CurrentUser Long userId,
                                        @NotBlank String query) {
        return fileService.search(userId, query);
    }

    @PostMapping("/resource")
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceInfoDto> upload(@CurrentUser Long userId,
                                        @ValidPath(mustBeDirectory = true) @RequestParam(required = false) String path,
                                        @RequestPart("object") List<MultipartFile> object) throws IOException {
        return fileService.upload(userId, path, object);
    }

    @GetMapping("/directory")
    public List<ResourceInfoDto> listDirectory(@CurrentUser Long userId,
                                               @RequestParam(required = false) @ValidPath(mustBeDirectory = true) String path) {
        return fileService.listFolder(userId, path);
    }

    @PostMapping("/directory")
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceInfoDto createDirectory(@CurrentUser Long userId,
                                           @RequestParam @ValidPath(mustBeDirectory = true) @NotBlank String path) {
        return fileService.createFolder(userId, path);
    }
}