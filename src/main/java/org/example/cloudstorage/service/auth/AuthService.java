package org.example.cloudstorage.service.auth;

import org.example.cloudstorage.dto.user.AuthRequestDto;
import org.example.cloudstorage.dto.user.AuthResponseDto;

public interface AuthService {

    AuthResponseDto login(AuthRequestDto dto);

    AuthResponseDto register(AuthRequestDto dto);
}
