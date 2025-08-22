package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.ResourceAlreadyExistsException;
import com.gross.cloudstorage.exception.ResourceNotFoundException;
import com.gross.cloudstorage.exception.ResourcePathValidationException;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
@Tag(name = "Операции с ресурсами")
public class ResourceController {
    private final MinioService minioService;

    public ResourceController(MinioService minioService) {
        this.minioService = minioService;
    }

    @Operation(summary = "Получить информацию о ресурсе")
    @GetMapping
    public ResponseEntity<?> getResource(@RequestParam(required = false) String path,
                                         Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            return ResponseEntity.ok(minioService.getResourceInformation(userDetails.getId(), path));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (ResourcePathValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        }catch (MinioServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> uploadResource(@RequestParam(required = false) String path,
                                            @RequestParam("object")MultipartFile object,
                                                    Authentication authentication)
    {
        System.out.println("uploadResource!!!!!   "+object.toString());
        System.out.println("Path!!!!! "+ path);
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try{
            minioService.uploadResource(userDetails.getId(), path, object);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (ResourcePathValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (MinioServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }


    @Operation(summary = "Удалить ресурс")
    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(required = false) String path,
                                            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            minioService.deleteResource(userDetails.getId(), path);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (ResourcePathValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (MinioServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}
