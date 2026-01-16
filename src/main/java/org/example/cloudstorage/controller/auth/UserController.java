package org.example.cloudstorage.controller.auth;

import org.example.cloudstorage.annotation.CurrentUser;
import org.example.cloudstorage.dto.AuthResponseDto;
import org.example.cloudstorage.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/me")
    public AuthResponseDto getCurrentUser(@CurrentUser User userDetails) {
        return new AuthResponseDto(userDetails.getUsername());
    }
}
