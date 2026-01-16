package org.example.cloudstorage.controller.storage;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.annotation.user.CurrentUser;
import org.example.cloudstorage.controller.swagger.DirectorySwagger;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;
import org.example.cloudstorage.service.storage.FileService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DirectoryController implements DirectorySwagger {

    private final FileService fileService;

    @Override
    public List<ResourceInfoDto> listDirectory(@CurrentUser Long userId, String path) {
        return fileService.listFolder(userId, path);
    }

    @Override
    public ResourceInfoDto createDirectory(@CurrentUser Long userId, String path) {
        return fileService.createFolder(userId, path);
    }
}
