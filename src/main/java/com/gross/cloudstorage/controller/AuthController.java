package com.gross.cloudstorage.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/sign-up")
    public String signUp() {

        return "Регистрация";
    }

    @PostMapping("/sign-in")
    public String signIn() {

        return "Авторизация";
    }

    @PostMapping("/sign-out")
    public String signOut() {

        return "Выход";
    }
}