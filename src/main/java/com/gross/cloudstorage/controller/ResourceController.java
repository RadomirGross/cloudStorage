package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.ResourceNotFoundException;
import com.gross.cloudstorage.exception.ResourcePathValidationException;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.CloudStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resource")
@Tag(name = "Операции с ресурсами")
public class ResourceController {
    private final CloudStorageService cloudStorageService;

    public ResourceController(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    @Operation(summary = "Получить информацию о ресурсе")
    @GetMapping
    public ResponseEntity<?> getResource(@RequestParam(required = false) String path,
                                         Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            return ResponseEntity.ok(cloudStorageService.getResourceInformation(userDetails.getId(), path));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (ResourcePathValidationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (MinioServiceException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Загрузить ресурс в облачное хранилище")
    @PostMapping
    public ResponseEntity<?> uploadResource(@RequestParam(required = false) String path,
                                            @RequestPart("object") List<MultipartFile> objects,
                                            Authentication authentication, HttpServletRequest request) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long contentLength = (Long) request.getAttribute("contentLength");
        return ResponseEntity.status(HttpStatus.CREATED).body(cloudStorageService.uploadResource(userDetails.getId(), path, objects, contentLength));
    }

    @Operation(summary = "Удалить ресурс")
    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(required = false) String path,
                                            Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        cloudStorageService.deleteResource(userDetails.getId(), path);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Переместить ресурс")
    @GetMapping("/move")
    public ResponseEntity<?> moveResource(@RequestParam(required = false) String from,
                                          @RequestParam(required = false) String to,
                                          Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok().body(cloudStorageService.moveResource(userDetails.getId(), from, to));
    }
}
