package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.exception.*;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.MinioService;
import com.gross.cloudstorage.utils.PathUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;
import java.util.Map;

@RestController
@RequestMapping("api/resource/download")
public class DownloadController {
    private final MinioService minioService;

    public DownloadController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping
    public ResponseEntity<?> download(@RequestParam(required = false) String path,
                                      Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        HttpHeaders headers = new HttpHeaders();
        boolean isDirectory = path.endsWith("/");
        String name = PathUtils.extractName(path);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + (isDirectory ? name + ".zip" : name) + "\"");
        InputStream inputStream = minioService.downloadResource(userDetails.getId(), path);
        try {
            return ResponseEntity.ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(new InputStreamResource(inputStream));
        } catch (PathValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
        } catch (MinioServiceException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }
}
