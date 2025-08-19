package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.security.CustomUserDetails;
import com.gross.cloudstorage.service.AuthService;
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
public class UserController {
    private final AuthService authService;

    public UserController(AuthService authService, AuthService authService1) {
        this.authService = authService1;
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
           if(!authService.isAuthenticatedUser(authentication)) {
               return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
           }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            System.out.println("principal " + userDetails.getUsername());

            return ResponseEntity.ok().body(Map.of("username", userDetails.getUsername()));
        } catch (ClassCastException e) {
            System.out.println("Principal is not CustomUserDetails: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}