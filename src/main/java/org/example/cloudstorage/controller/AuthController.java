package org.example.cloudstorage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.annotation.CurrentUser;
import org.example.cloudstorage.dto.AuthRequestDto;
import org.example.cloudstorage.dto.AuthResponseDto;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.service.auth.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final AuthService authService;

    @PostMapping("/auth/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDto signUp(@Valid @RequestBody AuthRequestDto dto) {
        authService.register(dto);
        return authService.login(dto);
    }

    @PostMapping("/auth/sign-in")
    public AuthResponseDto signIn(@Valid @RequestBody AuthRequestDto dto) {
        return authService.login(dto);
    }

    @PostMapping("/auth/sign-out")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void signOut() {}

    @GetMapping("/user/me")
    public AuthResponseDto getCurrentUser(@CurrentUser User userDetails) {
        return new AuthResponseDto(userDetails.getUsername());
    }
}
