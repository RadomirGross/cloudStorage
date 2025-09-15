package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.CloudStorageService;
import com.gross.cloudstorage.utils.PathUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@RestController
@RequestMapping("api/resource/download")
@Tag(name = "Скачивание ресурса")
public class DownloadController {
    private final CloudStorageService cloudStorageService;

    public DownloadController(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    @Operation(summary = "Скачать ресурс")
    @GetMapping
    public ResponseEntity<?> download(@RequestParam(required = false) String path,
                                      Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        boolean isDirectory = path.endsWith("/");
        String name = PathUtils.extractName(path);
        InputStream inputStream = cloudStorageService.downloadResource(userDetails.getId(), path);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + (isDirectory ? name + ".zip" : name) + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }
}
