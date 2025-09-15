package com.gross.cloudstorage.advice;

import com.gross.cloudstorage.exception.*;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {
    @Value("${spring.servlet.multipart.max-request-size}")
    private String maxRequestSize;
    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpectedException(Exception e) {
        logger.error("Unexpected error", e);
        return ResponseEntity
                .internalServerError()
                .body(Map.of("message", "Внутренняя ошибка сервера. Пожалуйста, попробуйте позже."));
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

    @ExceptionHandler({AuthenticationException.class, UserIsNotAuthenticatedException.class})
    public ResponseEntity<Map<String, String>> handleAuthentication(Exception e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({UserAlreadyExistsException.class, DirectoryAlreadyExistsException.class, ResourceAlreadyExistsException.class, ResourceConflictException.class})
    public ResponseEntity<Map<String, String>> handleConflict(Exception e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({ResourceNotFoundException.class, MissingParentFolderException.class, MissingParentFolderException.class})
    public ResponseEntity<Map<String, String>> handleNotFound(Exception e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({ResourcePathValidationException.class, UserValidationException.class, PathValidationException.class, ConflictingNameException.class})
    public ResponseEntity<Map<String, String>> handlePathValidation(Exception e) {
        return ResponseEntity.badRequest()
                .body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler({MinioServiceException.class, LogoutException.class, StorageQuotaExceededException.class})
    public ResponseEntity<Map<String, String>> handleInternalServerError(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", e.getMessage()));
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("message", errorMessage));
    }

}

