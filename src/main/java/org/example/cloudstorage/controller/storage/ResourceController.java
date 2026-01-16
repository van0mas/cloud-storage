package org.example.cloudstorage.controller.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.annotation.user.CurrentUser;
import org.example.cloudstorage.annotation.storage.ValidPath;
import org.example.cloudstorage.controller.swagger.ResourceSwagger;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;
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
@RequestMapping("/api/resource")
@RequiredArgsConstructor
@Validated
public class ResourceController implements ResourceSwagger {

    private final FileService fileService;

    @Override
    @GetMapping
    public ResourceInfoDto getResource(@CurrentUser Long userId,
                                       @RequestParam @ValidPath @NotBlank String path) {
        return fileService.getResource(userId, path);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public List<ResourceInfoDto> upload(@CurrentUser Long userId,
                                        @ValidPath(mustBeDirectory = true) @RequestParam(required = false) String path,
                                        @RequestPart("object") List<MultipartFile> object) throws IOException {
        return fileService.upload(userId, path, object);
    }

    @Override
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteResource(@CurrentUser Long userId,
                               @RequestParam @ValidPath @NotBlank String path) {
        fileService.delete(userId, path);
    }

    @Override
    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> download(@CurrentUser Long userId,
                                                          @RequestParam @ValidPath @NotBlank String path) {

        String fileName = PathUtils.getDownloadName(path);

        StreamingResponseBody responseBody = fileService.download(userId, path);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(responseBody);
    }

    @Override
    @GetMapping("/move")
    public ResourceInfoDto moveResource(@CurrentUser Long userId,
                                        @RequestParam @ValidPath @NotBlank String from,
                                        @RequestParam @ValidPath @NotBlank String to) {
        return fileService.move(userId, from, to);
    }

    @Override
    @GetMapping("/search")
    public List<ResourceInfoDto> search(@CurrentUser Long userId,
                                        @NotBlank String query) {
        return fileService.search(userId, query);
    }
}
