package com.gross.cloudstorage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class MinioObjectResponseDto {
    private final String path;
    private final String name;
    private final Long  size;
    private final ObjectType type;

    public enum ObjectType {
        FILE, DIRECTORY
    }
}
