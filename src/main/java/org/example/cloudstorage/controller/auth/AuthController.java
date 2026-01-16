package org.example.cloudstorage.controller.auth;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.AuthRequestDto;
import org.example.cloudstorage.dto.AuthResponseDto;
import org.example.cloudstorage.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto signUp(@Valid @RequestBody AuthRequestDto dto) {
        authService.register(dto);
        return authService.login(dto);
    }

    @PostMapping("/sign-in")
    public AuthResponseDto signIn(@Valid @RequestBody AuthRequestDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut() {}
}
