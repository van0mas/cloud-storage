package org.example.cloudstorage.controller.storage;

import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.annotation.user.CurrentUser;
import org.example.cloudstorage.annotation.storage.ValidPath;
import org.example.cloudstorage.controller.swagger.DirectorySwagger;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;
import org.example.cloudstorage.service.storage.FileService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directory")
@RequiredArgsConstructor
@Validated
public class DirectoryController implements DirectorySwagger {

    private final FileService fileService;

    @Override
    @GetMapping
    public List<ResourceInfoDto> listDirectory(@CurrentUser Long userId,
                                               @RequestParam(required = false) @ValidPath(mustBeDirectory = true) String path) {
        return fileService.listFolder(userId, path);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResourceInfoDto createDirectory(@CurrentUser Long userId,
                                           @RequestParam @ValidPath(mustBeDirectory = true) @NotBlank String path) {
        return fileService.createFolder(userId, path);
    }
}
