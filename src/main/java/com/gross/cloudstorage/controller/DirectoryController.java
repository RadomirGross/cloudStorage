package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.DirectoryAlreadyExistsException;
import com.gross.cloudstorage.exception.PathValidationException;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.MissingParentFolderException;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/directory")
@Tag(name = "Операции с директориями")
public class DirectoryController {
    private final MinioService minioService;

    public DirectoryController(MinioService minioService) {
        this.minioService = minioService;
    }

    @Operation(summary = "Получить содержимое директории")
    @GetMapping
    public ResponseEntity<?> getFolder
            (@RequestParam(required = false) String path,
             Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<MinioObjectResponseDto> objects =
                minioService.getUserFolder(userDetails.getId(), path);
        return ResponseEntity.ok(objects);
    }

    @Operation(summary = "Создать новую директорию")
    @PostMapping
    public ResponseEntity<?> createFolder(@RequestParam(required = false) String path,
                                          Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(minioService.createDirectory(userDetails.getId(), path,false));
        } catch (MissingParentFolderException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (PathValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (DirectoryAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage()));
        } catch (MinioServiceException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
}
