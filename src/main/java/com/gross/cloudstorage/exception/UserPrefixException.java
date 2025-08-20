package com.gross.cloudstorage.exception;

public class UserPrefixException extends RuntimeException {
    public  UserPrefixException(String message) {
        super(message);
    }
    public UserPrefixException(String message, Throwable cause) {}
}
