package com.gross.cloudstorage.exception;

public class MissingParentFolderException extends RuntimeException {
    public MissingParentFolderException(String message) {
        super(message);
    }
}
