package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.MinioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Tag(name = "Операции с директориями")
public class DirectoryController {
    private final MinioService minioService;

    public DirectoryController(MinioService minioService) {
        this.minioService = minioService;
    }

    @Operation(summary = "Получить содержимое директории")
    @GetMapping("api/directory")
    public ResponseEntity<List<MinioObjectResponseDto>> getFolder
            (@RequestParam(required = false) String path,
             Authentication authentication) {

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        List<MinioObjectResponseDto> objects =
                minioService.getUserFolder(userDetails.getId(), path);
        return ResponseEntity.ok(objects);
    }

    @Operation(summary = "Создать новую директорию")
    @PostMapping("api/directory")
    public MinioObjectResponseDto createFolder(@RequestParam(required = false) String path,
                                               Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return minioService.createFolder(userDetails.getId(), path);
    }
}
