package org.example.cloudstorage.exception.user;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.USER_ALREADY_EXISTS;

public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException() {
        super(USER_ALREADY_EXISTS);
    }
}
