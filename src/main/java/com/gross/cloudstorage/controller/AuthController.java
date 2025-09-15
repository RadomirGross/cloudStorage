package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.AuthRequestDto;
import com.gross.cloudstorage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Методы регистрации и логина")
@Validated
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@Valid @RequestBody AuthRequestDto authRequestDto,
                                    HttpServletRequest httpRequest) {
        String username = authService.register(authRequestDto, httpRequest);
        return ResponseEntity.ok(Map.of("username", username));
    }

    @Operation(summary = "Авторизация")
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@Valid @RequestBody AuthRequestDto authRequestDto, HttpServletRequest httpRequest) {
        String username = authService.authenticate(authRequestDto, httpRequest);
        return ResponseEntity.ok(Map.of("username", username));
    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }


}