package com.gross.cloudstorage.exception;

public class ConflictingNameException extends RuntimeException {
    public ConflictingNameException(String message) {
        super(message);
    }
}
