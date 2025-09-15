package com.gross.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AuthRequestDto {

    @NotBlank(message = "Имя пользователя не может быть пустым")
    @Size(min = 5, max = 20, message = "Длина имени пользователя должна быть от 5 до 20 символов")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 5, max = 20, message = "Длина пароля должна быть от 5 до 20 символов")
    private String password;
}
