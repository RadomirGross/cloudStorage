package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.AuthRequestDto;
import com.gross.cloudstorage.exception.*;
import com.gross.cloudstorage.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Методы регистрации и логина")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody AuthRequestDto authRequestDto,
                                     HttpServletRequest httpRequest) {
        try {
            String username = authService.register(authRequestDto, httpRequest);
            return ResponseEntity.ok(Map.of("username", username));
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",
                    "Пользователь с таким именем уже существует"));
        } catch (UserValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message",
                    "Ошибка при аутентификации"));
        } catch (MinioServiceException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }

    @Operation(summary = "Авторизация")
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody AuthRequestDto authRequestDto, HttpServletRequest httpRequest) {
        try {
            String username = authService.authenticate(authRequestDto, httpRequest);
            return ResponseEntity.ok(Map.of("username", username));
        } catch (UserValidationException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Неверный логин или пароль"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Неизвестная ошибка"));
        }

    }

    @PostMapping("/sign-out")
    public ResponseEntity<?> signOut(HttpServletRequest request) {
        try {
            authService.logout(request);
            return ResponseEntity.noContent().build();
        } catch (UserIsNotAuthenticatedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", e.getMessage()));
        } catch (LogoutException e) {
            return ResponseEntity.internalServerError().body(Map.of("message", e.getMessage()));
        }
    }


}