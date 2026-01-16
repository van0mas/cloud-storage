package org.example.cloudstorage.controller.swagger;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.cloudstorage.dto.user.AuthRequestDto;
import org.example.cloudstorage.dto.user.AuthResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Authorization", description = "Регистрация, вход и выход из системы")
@RequestMapping("/api/auth")
public interface AuthSwagger {

    @Operation(
            summary = "Регистрация",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Успешная регистрация"),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким логином уже существует")
            }
    )
    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    AuthResponseDto signUp(
            @Valid
            @RequestBody
            AuthRequestDto dto
    );

    @Operation(
            summary = "Вход",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешный вход"),
                    @ApiResponse(responseCode = "401", description = "Неверный логин или пароль")
            }
    )
    @PostMapping("/sign-in")
    AuthResponseDto signIn(
            @Valid
            @RequestBody
            AuthRequestDto dto
    );

    @Operation(
            summary = "Выход из системы",
            description = "Завершает текущую сессию и удаляет куки",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Успешный выход")
            }
    )
    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void signOut();
}

