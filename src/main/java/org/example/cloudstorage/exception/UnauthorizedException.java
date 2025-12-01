package org.example.cloudstorage.exception;

public class UnauthorizedException extends RuntimeException {
    public  UnauthorizedException(String message) {
        super(message);
    }
}
