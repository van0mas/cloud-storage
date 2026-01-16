package org.example.cloudstorage.controller.user;

import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.controller.swagger.AuthSwagger;
import org.example.cloudstorage.dto.user.AuthRequestDto;
import org.example.cloudstorage.dto.user.AuthResponseDto;
import org.example.cloudstorage.service.auth.AuthService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthSwagger {

    private final AuthService authService;

    @Override
    public AuthResponseDto signUp(AuthRequestDto dto) {
        authService.register(dto);
        return authService.login(dto);
    }

    @Override
    public AuthResponseDto signIn(AuthRequestDto dto) {
        return authService.login(dto);
    }

    @Override
    public void signOut() {
    }
}
