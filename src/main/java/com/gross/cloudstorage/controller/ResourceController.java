package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.ResourceNotFoundException;
import com.gross.cloudstorage.exception.ResourcePathValidationException;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/resource")
@Tag(name = "Операции с ресурсами")
public class ResourceController {
    private final MinioService minioService;

    public ResourceController(MinioService minioService) {
        this.minioService = minioService;
    }
    @Operation(summary = "Удалить ресурс")
    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(required = false) String path,
                                            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            System.out.println("ResourceController "+path);
            minioService.deleteResource(userDetails.getId(), path);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        }catch (ResourcePathValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }catch (MinioServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}
