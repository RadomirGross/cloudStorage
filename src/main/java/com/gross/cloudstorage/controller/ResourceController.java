package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.*;
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
import java.util.ArrayList;
import java.util.List;
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
        } catch (MinioServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> uploadResource(@RequestParam(required = false) String path,
                                            @RequestParam("object") List<MultipartFile> objects,
                                            Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(minioService.uploadResources(userDetails.getId(), path, objects));
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

    @GetMapping("/move")
    public ResponseEntity<?> moveResource(@RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to,
                                          Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            return ResponseEntity.ok().body(minioService.moveResource(userDetails.getId(), from, to));
        } catch (PathValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message","Ошибка при перемещении."+ e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Ошибка при перемещении."+e.getMessage()));
        } catch (ResourceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Ошибка при перемещении."+ e.getMessage()));
        } catch (MinioServiceException e) {
            return ResponseEntity.internalServerError().body(Map.of("message","Ошибка при перемещении."+ e.getMessage()));
        }
    }
}
