package org.example.cloudstorage.exception.user;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.USER_NOT_FOUND;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super(USER_NOT_FOUND);
    }
}
