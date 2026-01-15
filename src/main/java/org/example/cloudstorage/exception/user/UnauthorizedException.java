package org.example.cloudstorage.exception.user;

import static org.example.cloudstorage.config.AppConstants.ExceptionMessages.UNAUTHORIZED;

public class UnauthorizedException extends RuntimeException {
    public UnauthorizedException() {
        super(UNAUTHORIZED);
    }
}
