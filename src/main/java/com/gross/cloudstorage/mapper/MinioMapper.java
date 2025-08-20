package com.gross.cloudstorage.mapper;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.UserPrefixException;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.messages.Item;

import java.util.ArrayList;
import java.util.List;


public class MinioMapper {


    public static MinioObjectResponseDto toDtoMinioObject(Item item,long userId) {
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
                PathUtils.stripUserPrefix(item.objectName(),userId),
                PathUtils.stripUserPrefix(path,userId),
                name,
                isDirectory ? null : item.size(),
                isDirectory ? MinioObjectResponseDto.ObjectType.DIRECTORY : MinioObjectResponseDto.ObjectType.FILE
        );
    }

    public String deleteUserPrefix(long userId, String objectName) {
        String prefix = "user-" + userId + "-files/";
        if (objectName.startsWith(prefix)) {
            return objectName.substring(prefix.length());
        }
        throw new UserPrefixException("Ошибка при удалении префикса");
    }


    public static List<MinioObjectResponseDto> toListDtoMinioObject(List<Item> minioItems,long userId) {
        List<MinioObjectResponseDto> list = new ArrayList<>();
        for (Item item : minioItems) {
            list.add(toDtoMinioObject(item,userId));
        }

        return list;
    }


    public static MinioObjectResponseDto toDtoMinioObjectJustForDirectory(String objectName,long userId) {
        if (!objectName.endsWith("/")) {
            throw new MinioServiceException("Путь директории должен заканчиваться на /");
        }

        String[] parts = objectName.split("/");

        String name = parts[parts.length - 1] + "/";
        String path = objectName.substring(0, objectName.length() - name.length());

        return new MinioObjectResponseDto(
                PathUtils.stripUserPrefix(objectName,userId),
                PathUtils.stripUserPrefix(path,userId),
                name,
                null,
                MinioObjectResponseDto.ObjectType.DIRECTORY
        );
    }


}
