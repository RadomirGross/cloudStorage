package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Аутентификация", description = "Методы регистрации и логина")
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService, AuthService authService1) {
        this.authService = authService1;
    }

    @Operation(summary = "Получить зарегистрированного пользователя")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
           if(!authService.isAuthenticatedUser(authentication)) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();}

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            return ResponseEntity.ok().body(Map.of("username", userDetails.getUsername()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", e.getMessage()));
        }
    }
}