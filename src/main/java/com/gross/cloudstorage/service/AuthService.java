package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.AuthRequestDto;
import com.gross.cloudstorage.exception.LogoutException;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.UserAlreadyExistsException;
import com.gross.cloudstorage.exception.UserIsNotAuthenticatedException;
import com.gross.cloudstorage.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private  final SecurityContextRepository securityContextRepository;
    private final MinioService minioService;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager, SecurityContextRepository securityContextRepository, MinioService minioService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.minioService = minioService;
    }

    @Transactional
    public String register(AuthRequestDto authRequestDto, HttpServletRequest httpRequest) {
        if (userService.exists(authRequestDto.getUsername())) {
            throw new UserAlreadyExistsException("User " + authRequestDto.getUsername() + " already exists");
        }

        userService.validateUser(authRequestDto.getUsername(), authRequestDto.getPassword());
        User user = userService.createUser(authRequestDto, passwordEncoder);
        userService.register(user);

        try {
            minioService.createFolder(user.getId(),"");
        } catch (MinioServiceException e) {
            userService.deleteUser(user);
            throw e;
        }
        return authenticate(authRequestDto, httpRequest);
    }

    public String authenticate(AuthRequestDto authRequestDto, HttpServletRequest httpRequest) {
        userService.validateUser(authRequestDto.getUsername(), authRequestDto.getPassword());
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(authRequestDto.getUsername(), authRequestDto.getPassword());
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(context, httpRequest, null);

        return authRequestDto.getUsername();

    }

    public void logout(HttpServletRequest httpRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAuthenticatedUser(authentication)) {
            throw new UserIsNotAuthenticatedException("User is not authenticated");
        }

        try {
            httpRequest.logout();
            SecurityContextHolder.clearContext();
        } catch (ServletException e) {
            throw new LogoutException("Logout failed" + e.getMessage());
        }
    }

    public boolean isAuthenticatedUser(Authentication authentication) {
        return authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
