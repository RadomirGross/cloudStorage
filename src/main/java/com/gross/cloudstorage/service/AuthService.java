package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.AuthRequestDto;
import com.gross.cloudstorage.exception.LogoutException;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.UserIsNotAuthenticatedException;
import com.gross.cloudstorage.mapper.UserMapper;
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


@Service
public class AuthService {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final CloudStorageService cloudStorageService;
    private final UserMapper userMapper;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       SecurityContextRepository securityContextRepository,
                       CloudStorageService cloudStorageService, UserMapper userMapper) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
        this.cloudStorageService = cloudStorageService;
        this.userMapper = userMapper;
    }

    public String register(AuthRequestDto authRequestDto, HttpServletRequest httpRequest) {

        User user = userService.create(authRequestDto);

        try {
            cloudStorageService.createDirectory(user.getId(), "", true);
        } catch (MinioServiceException e) {
            userService.deleteUser(user);
            throw e;
        }
        return authenticate(authRequestDto, httpRequest);
    }

    public String authenticate(AuthRequestDto authRequestDto, HttpServletRequest httpRequest) {
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
            throw new UserIsNotAuthenticatedException("Пользователь на аутентифицирован");
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
