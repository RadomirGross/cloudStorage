package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.exception.PathValidationException;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.CloudStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/resource/search")
@Tag(name = "Операции с ресурсами")
public class SearchController {
    private final CloudStorageService cloudStorageService;

    public SearchController(CloudStorageService cloudStorageService) {
        this.cloudStorageService = cloudStorageService;
    }

    @Operation(summary = "Поиск ресурса")
    @GetMapping
    public ResponseEntity<?> search(@RequestParam(required = false) String query, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            return ResponseEntity.ok().body(cloudStorageService.searchResources(userDetails.getId(), query));
        } catch (PathValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}