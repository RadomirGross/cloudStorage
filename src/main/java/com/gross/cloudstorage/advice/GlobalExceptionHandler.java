package com.gross.cloudstorage.advice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

import java.util.Map;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String,String>> handleUnexpectedException(Exception e) {
        logger.error("Unexpected error",e);
        return  ResponseEntity
                .internalServerError()
                .body(Map.of("message","Внутренняя ошибка сервера. Пожалуйста, попробуйте позже."));
    }

    @ExceptionHandler({MaxUploadSizeExceededException.class, SizeLimitExceededException.class, FileSizeLimitExceededException.class})
    public ResponseEntity<Map<String, String>> handleMaxSizeException(Exception e) {
        logger.warn("Upload size exceeded:", e);
        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", "Превышен максимальный размер загружаемого файла или запроса. " +
                        "(Максимальный размер файла - " + maxFileSize + ", запроса - " + maxRequestSize + ")"));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateForUpload(IllegalStateException e) {
        // Some containers wrap size exceed into IllegalStateException
        Throwable cause = e.getCause();
        if (cause instanceof SizeLimitExceededException ||
                cause instanceof FileSizeLimitExceededException ) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Превышен максимальный размер загружаемого файла или запроса. " +
                            "(Максимальный размер файла - " + maxFileSize + ", запроса - " + maxRequestSize + ")"));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", "Некорректное состояние запроса"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<Map<String, String>> handleMultipartException(MultipartException e) {
        logger.warn("Multipart exception:", e);
        Throwable cause = e.getMostSpecificCause();
        if (cause instanceof MaxUploadSizeExceededException ||
                cause instanceof SizeLimitExceededException ||
                cause instanceof FileSizeLimitExceededException) {
            return ResponseEntity
                    .status(HttpStatus.PAYLOAD_TOO_LARGE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("message", "Превышен максимальный размер загружаемого файла или запроса. " +
                            "(Максимальный размер файла - " + maxFileSize + ", запроса - " + maxRequestSize + ")"));
        }
        return ResponseEntity
                .badRequest()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("message", "Ошибка загрузки файла"));
    }
}

