package org.example.cloudstorage.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.example.cloudstorage.annotation.storage.ValidPath;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Directory", description = "Навигация и управление структурой папок")
@RequestMapping("/api/directory")
public interface DirectorySwagger {

    @Operation(
            summary = "Просмотр содержимого папки",
            description = "Возвращает список файлов и подпапок по указанному пути. Если путь пустой, возвращает корень.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Список получен",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceInfoDto.class)))
                    ),
                    @ApiResponse(responseCode = "400", description = "Некорректный путь или объект не является папкой"),
                    @ApiResponse(responseCode = "401", description = "Пользователь не авторизован")
            }
    )
    @GetMapping
    List<ResourceInfoDto> listDirectory(
            Long userId,
            @RequestParam(required = false)
            @ValidPath(mustBeDirectory = true)
            String path
    );

    @Operation(
            summary = "Создание новой папки",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Папка создана"),
                    @ApiResponse(responseCode = "400", description = "Некорректный путь"),
                    @ApiResponse(responseCode = "409", description = "Папка по такому пути уже существует")
            }
    )
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResourceInfoDto createDirectory(
            Long userId,
            @RequestParam
            @NotBlank
            @ValidPath(mustBeDirectory = true)
            String path
    );
}

