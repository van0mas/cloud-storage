package org.example.cloudstorage.controller.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.annotation.CurrentUser;
import org.example.cloudstorage.annotation.ValidPath;
import org.example.cloudstorage.dto.ResourceInfoDto;
import org.example.cloudstorage.service.storage.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Validated
public class DirectoryController {

    private final FileService fileService;

    @GetMapping
    public List<ResourceInfoDto> listDirectory(@CurrentUser Long userId,
                                               @RequestParam(required = false) @ValidPath(mustBeDirectory = true) String path) {
        return fileService.listFolder(userId, path);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceInfoDto createDirectory(@CurrentUser Long userId,
                                           @RequestParam @ValidPath(mustBeDirectory = true) @NotBlank String path) {
        return fileService.createFolder(userId, path);
    }
}
