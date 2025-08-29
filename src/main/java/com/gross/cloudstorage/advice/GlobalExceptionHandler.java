package com.gross.cloudstorage.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleUnexpectedException(Exception e) {
        logger.error("Unexpected error",e);
        return  ResponseEntity
                .internalServerError()
                .body(Map.of("message","Внутренняя ошибка сервера. Пожалуйста, попробуйте позже."));
    }
}
