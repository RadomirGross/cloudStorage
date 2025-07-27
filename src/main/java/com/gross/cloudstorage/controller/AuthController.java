package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.dto.AuthRequest;
import com.gross.cloudstorage.model.User;
import com.gross.cloudstorage.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "Методы регистрации и логина")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthController(UserService userService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    @Operation(summary = "Регистрация нового пользователя")
    @PostMapping("/sign-up")
    public ResponseEntity<?> getUser(@RequestBody AuthRequest request,
                                     HttpServletRequest httpRequest) {
        if (userService.exists(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message",
                    "Username " + request.getUsername() + " is already exists"));
        }
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userService.register(user);
        return authenticateAndReturn(request, httpRequest);
    }

    @Operation(summary = "Авторизация")
    @PostMapping("/sign-in")
    public ResponseEntity<?> signIn(@RequestBody AuthRequest request, HttpServletRequest httpRequest) {
        return authenticateAndReturn(request, httpRequest);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated() || !
                    (authentication instanceof AnonymousAuthenticationToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            request.logout();
            return ResponseEntity.noContent().build();
        } catch (ServletException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseEntity<?> authenticateAndReturn(AuthRequest request, HttpServletRequest httpRequest) {
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
            Authentication authentication = authenticationManager.authenticate(authToken);

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
            securityContextRepository.saveContext(context, httpRequest, null);

            System.out.println("✅ Пользователь авторизован: " + authentication.getName());

            return ResponseEntity.ok(Map.of("username", authentication.getName()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"));
        }
    }
}