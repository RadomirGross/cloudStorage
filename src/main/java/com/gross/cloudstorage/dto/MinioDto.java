package com.gross.cloudstorage.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@ToString
public class MinioDto {
    private final String path;
    private final String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private final Long size;
    private final ObjectType type;

    public enum ObjectType {
        FILE, DIRECTORY
    }
}
