package org.example.cloudstorage.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cloudstorage.dto.user.AuthResponseDto;
import org.example.cloudstorage.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "User", description = "Управление данными пользователя и профилем")
@RequestMapping("/api/user")
public interface UserSwagger {

    @Operation(
            summary = "Получить данные текущего пользователя",
            description = "Возвращает информацию об авторизованном пользователе на основе сессии",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Данные успешно получены",
                            content = @Content(schema = @Schema(implementation = AuthResponseDto.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизован"
                    )
            }
    )
    @GetMapping("/me")
    AuthResponseDto getCurrentUser(User userDetails);
}

