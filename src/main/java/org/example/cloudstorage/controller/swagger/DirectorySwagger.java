package org.example.cloudstorage.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;

import java.util.List;

@Tag(name = "Directory", description = "Навигация и управление структурой папок")
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
    List<ResourceInfoDto> listDirectory(
            Long userId,
            @Parameter(description = "Путь к папке (например, 'documents/work'). Оставьте пустым для корня.", example = "photos/summer") String path
    );

    @Operation(summary = "Создание новой папки", responses = {
            @ApiResponse(responseCode = "201", description = "Папка создана"),
            @ApiResponse(responseCode = "400", description = "Некорректный путь"),
            @ApiResponse(responseCode = "409", description = "Папка по такому пути уже существует")
    })
    ResourceInfoDto createDirectory(Long userId, String path);
}
