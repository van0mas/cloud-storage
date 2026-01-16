package org.example.cloudstorage.controller.user;

import org.example.cloudstorage.annotation.user.CurrentUser;
import org.example.cloudstorage.controller.swagger.UserSwagger;
import org.example.cloudstorage.dto.user.AuthResponseDto;
import org.example.cloudstorage.model.User;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController implements UserSwagger {

    @Override
    public AuthResponseDto getCurrentUser(@CurrentUser User userDetails) {
        return new AuthResponseDto(userDetails.getUsername());
    }
}
