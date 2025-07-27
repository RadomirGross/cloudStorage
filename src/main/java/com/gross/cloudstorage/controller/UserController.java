package com.gross.cloudstorage.controller;

import com.gross.cloudstorage.security.CustomUserDetails;
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

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            System.out.println("/me endpoint");
            if (authentication == null || !authentication.isAuthenticated() ||
                    authentication instanceof AnonymousAuthenticationToken) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            System.out.println("principal " + userDetails.getUsername());

            Map<String, Object> response = Map.of(
                    "username", userDetails.getUsername()
            );

            return ResponseEntity.ok(response);
        } catch (ClassCastException e) {
            System.err.println("Principal is not CustomUserDetails: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}