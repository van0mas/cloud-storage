package org.example.cloudstorage.service.auth;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.example.cloudstorage.dto.AuthRequestDto;
import org.example.cloudstorage.dto.AuthResponseDto;
import org.example.cloudstorage.exception.user.UnauthorizedException;
import org.example.cloudstorage.mapper.AuthMapper;
import org.example.cloudstorage.model.User;
import org.example.cloudstorage.service.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthMapper authMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final HttpServletRequest request;

    @Override
    public AuthResponseDto login(AuthRequestDto dto) {
        UsernamePasswordAuthenticationToken token
                = new UsernamePasswordAuthenticationToken(dto.getUsername(), dto.getPassword());

        Authentication auth = authenticationManager.authenticate(token);

        SecurityContextHolder.getContext().setAuthentication(auth);
        request.getSession(true).setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );

        User user = userService.findByUsername(dto.getUsername());
        return authMapper.toResponseDto(user);
    }

    @Override
    public AuthResponseDto register(AuthRequestDto dto) {
        User user = authMapper.toEntity(dto);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User savedUser = userService.createUser(user);
        return authMapper.toResponseDto(savedUser);
    }

    @Override
    public void logout() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException();
        }

        SecurityContextHolder.clearContext();
    }
}
