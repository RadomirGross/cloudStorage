package com.gross.cloudstorage.security;

import com.gross.cloudstorage.service.CustomUserDetailsService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collections;

//Этот класс можно удалить

public class AuthProviderImpl implements AuthenticationProvider {

    private final CustomUserDetailsService customUserDetailsService;

    public AuthProviderImpl(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName();
        UserDetails userDetails= customUserDetailsService.loadUserByUsername(username);

        String password=authentication.getCredentials().toString();
        if (!password.equals(userDetails.getPassword()))
        {
            throw new BadCredentialsException("Неверный пароль или имя пользователя");
        }
        return new UsernamePasswordAuthenticationToken(userDetails, password, Collections.emptyList());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
