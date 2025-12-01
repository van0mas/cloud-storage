package org.example.cloudstorage.service;

import org.example.cloudstorage.dto.AuthRequestDto;
import org.example.cloudstorage.dto.AuthResponseDto;

public interface AuthService {

    AuthResponseDto login(AuthRequestDto dto);

    AuthResponseDto register(AuthRequestDto dto);

    void logout();
}
