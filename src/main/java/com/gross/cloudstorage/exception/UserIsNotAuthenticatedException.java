package com.gross.cloudstorage.exception;

public class UserIsNotAuthenticatedException extends RuntimeException {
    public UserIsNotAuthenticatedException(String message) {
        super(message);
    }
}
