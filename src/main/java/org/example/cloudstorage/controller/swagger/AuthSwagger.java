package org.example.cloudstorage.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.cloudstorage.dto.user.AuthRequestDto;
import org.example.cloudstorage.dto.user.AuthResponseDto;

@Tag(name = "Authorization", description = "Регистрация, вход и выход из системы")
public interface AuthSwagger {

    @Operation(summary = "Регистрация", responses = {
            @ApiResponse(responseCode = "201", description = "Успешная регистрация"),
            @ApiResponse(responseCode = "409", description = "Пользователь с таким логином уже существует")
    })
    AuthResponseDto signUp(AuthRequestDto dto);

    @Operation(summary = "Вход", responses = {
            @ApiResponse(responseCode = "200", description = "Успешный вход"),
            @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
    })

    AuthResponseDto signIn(AuthRequestDto dto);

    @Operation(
            summary = "Выход из системы",
            description = "Завершает текущую сессию и удаляет куки",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Успешный выход")
            }
    )
    void signOut();
}
