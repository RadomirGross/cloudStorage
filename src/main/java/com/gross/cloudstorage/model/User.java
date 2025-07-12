package com.gross.cloudstorage.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 3,max = 30,message = "Логин должен быть от 3 до 30 симво    лов")
    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;
}
