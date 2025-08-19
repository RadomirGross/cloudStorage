package com.gross.cloudstorage.mapper;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import io.minio.messages.Item;

import java.util.ArrayList;
import java.util.List;


public class MinioMapper {


    public static MinioObjectResponseDto toDtoMinioObject(Item item) {
        String objectName = item.objectName();
        boolean isDirectory = objectName.endsWith("/");
        String[] parts = objectName.split("/");

        String name;
        if (isDirectory) {
            if (parts.length > 1) {
                name = parts[parts.length - 1] + "/";
            } else {
                name = "";
            }
        } else {
            name = parts[parts.length - 1];
        }

        String path = objectName.substring(0, objectName.length() - name.length());

        return new MinioObjectResponseDto(
                item.objectName(),
                path,
                name,
                isDirectory ? null : item.size(),
                isDirectory ? MinioObjectResponseDto.ObjectType.DIRECTORY : MinioObjectResponseDto.ObjectType.FILE
        );
    }


    public static List<MinioObjectResponseDto> toListDtoMinioObject(List<Item> minioItems) {
        List<MinioObjectResponseDto> list = new ArrayList<>();
        for (Item item : minioItems) {
            list.add(toDtoMinioObject(item));
        }
        return list;
    }


    public static MinioObjectResponseDto toDtoMinioObjectJustForDirectory(String objectName) {
        if (!objectName.endsWith("/")) {
            throw new MinioServiceException("Путь директории должен заканчиваться на /");
        }

        String[] parts = objectName.split("/");

        String name = parts[parts.length - 1] + "/";
        String path = objectName.substring(0, objectName.length() - name.length());

        return new MinioObjectResponseDto(
                objectName,
                path,
                name,
                null,
                MinioObjectResponseDto.ObjectType.DIRECTORY
        );
    }


}
