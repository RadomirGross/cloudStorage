package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.MinioService;
import com.sun.security.auth.UserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DirectoryController {
    private final MinioService minioService;

    public DirectoryController(MinioService minioService) {
        this.minioService = minioService;
    }

    @GetMapping("api/directory")
    public ResponseEntity<List<MinioObjectResponseDto>> getFolder
            (@RequestParam(required = false) String path,
                    Authentication authentication) {

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String userFolder = "user-" + principal.ge



        List<MinioObjectResponseDto> objects = minioService.getFolder(path);
        System.out.println("api/directory");
        for (MinioObjectResponseDto object : objects) {
            System.out.println(object);
        }
        return ResponseEntity.ok(objects);
    }
}
