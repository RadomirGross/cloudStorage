package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.exception.PathValidationException;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.MinioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/resource/search")
public class SearchController {
    private final MinioService minioService;

    public SearchController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping
    public ResponseEntity<?> search(@RequestParam(required = false) String query, Authentication authentication) {
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        try {
            return ResponseEntity.ok().body(minioService.searchResources(userDetails.getId(), query));
        } catch (PathValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}