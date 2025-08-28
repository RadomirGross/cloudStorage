package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.exception.*;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.CloudStorageService;
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
    private final CloudStorageService cloudStorageService;

    public DirectoryController(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    @Operation(summary = "Получить содержимое директории")
    @GetMapping
    public ResponseEntity<?> getFolder
            (@RequestParam(required = false) String path,
             Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            List<MinioDto> objects =
                    cloudStorageService.getUserDirectory(userDetails.getId(), path);
            return ResponseEntity.ok(objects);
        } catch (PathValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message","Ошибка при получении содержимого. "+e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Ошибка при получении содержимого. " + e.getMessage()));
        } catch (MinioServiceException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Ошибка при получении содержимого. " + e.getMessage()));
        }
    }

        @Operation(summary = "Создать новую директорию")
        @PostMapping
        public ResponseEntity<?> createFolder (@RequestParam(required = false) String path,
                Authentication authentication){
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            try {
                return ResponseEntity.status(HttpStatus.CREATED).body(cloudStorageService.createDirectory(userDetails.getId(), path));
            } catch (MissingParentFolderException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message","Ошибка при создании директории. "+ e.getMessage()));
            } catch (PathValidationException e) {
                return ResponseEntity.badRequest().body(Map.of("message","Ошибка при создании директории. "+ e.getMessage()));
            } catch (DirectoryAlreadyExistsException e) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message","Ошибка при создании директории. "+ e.getMessage()));
            } catch (MinioServiceException e) {
                return ResponseEntity.internalServerError().body(Map.of("message","Ошибка при создании директории. "+ e.getMessage()));
            }
        }
    }
