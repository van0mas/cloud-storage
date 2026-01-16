package org.example.cloudstorage.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cloudstorage.dto.storage.ResourceInfoDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.List;

@Tag(name = "Resource", description = "Действия над файлами и папками")
public interface ResourceSwagger {

    @Operation(summary = "Получить информацию об объекте", responses = {
            @ApiResponse(responseCode = "200", description = "Информация получена"),
            @ApiResponse(responseCode = "404", description = "Объект по указанному пути не найден")
    })
    ResourceInfoDto getResource(Long userId, String path);

    @Operation(summary = "Загрузка файлов", responses = {
            @ApiResponse(responseCode = "201", description = "Файлы успешно загружены"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации пути или пустой файл"),
            @ApiResponse(responseCode = "409", description = "Файл с таким именем уже существует в данной папке")
    })
    List<ResourceInfoDto> upload(Long userId, String path, List<MultipartFile> object) throws IOException;

    @Operation(summary = "Удаление объекта", responses = {
            @ApiResponse(responseCode = "204", description = "Успешно удалено"),
            @ApiResponse(responseCode = "404", description = "Файл или папка не найдены")
    })
    void deleteResource(Long userId, String path);

    @Operation(summary = "Перемещение или переименование", responses = {
            @ApiResponse(responseCode = "200", description = "Объект успешно перемещен"),
            @ApiResponse(responseCode = "404", description = "Исходный объект не найден"),
            @ApiResponse(responseCode = "409", description = "В целевой папке уже есть объект с таким именем")
    })
    ResourceInfoDto moveResource(Long userId, String from, String to);
    @Operation(
            summary = "Скачать файл",
            description = "Возвращает содержимое файла в виде потока данных. Устанавливает правильный Content-Disposition для браузера.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Файл найден и передается",
                            content = @Content(mediaType = "application/octet-stream")
                    ),
                    @ApiResponse(responseCode = "404", description = "Файл не найден")
            }
    )
    ResponseEntity<StreamingResponseBody> download(
            Long userId,
            @Parameter(description = "Путь к файлу для скачивания", example = "docs/contract.pdf") String path
    );

    @Operation(
            summary = "Поиск файлов и папок",
            description = "Рекурсивный поиск по названию среди всех ресурсов пользователя.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Результаты поиска (может быть пустым списком)",
                            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ResourceInfoDto.class)))
                    ),
                    @ApiResponse(responseCode = "401", description = "Неавторизован")
            }
    )
    List<ResourceInfoDto> search(
            Long userId,
            @Parameter(description = "Часть имени файла или папки для поиска", example = "report") String query
    );
}