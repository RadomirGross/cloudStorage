package com.gross.cloudstorage.mapper;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class MinioMapper {


    public static MinioObjectResponseDto  toDtoMinioObject(Item item, long userId) {
        String objectName = item.objectName();
        return createDto(userId, objectName, item.size());
    }

    public static MinioObjectResponseDto  toDtoMinioObject(String objectName,Long size, long userId) {
        return createDto(userId, objectName, size);
    }


    public static List<MinioObjectResponseDto> toListDtoMinioObject(List<Item> minioItems, long userId) {
        List<MinioObjectResponseDto> list = new ArrayList<>();
        for (Item item : minioItems) {
            list.add(toDtoMinioObject(item, userId));
        }
        return list;
    }


    public static MinioObjectResponseDto toDtoMinioObjectJustForDirectory(String objectName, long userId) {
        if (!objectName.endsWith("/")) {
            throw new MinioServiceException("Путь директории должен заканчиваться на /");
        }
        return createDto(userId, objectName, null);
    }

    public static MinioObjectResponseDto toDtoMinioObjectFromStat(StatObjectResponse statObjectResponse, long userId) {
        String objectName = statObjectResponse.object();
        return createDto(userId, objectName, statObjectResponse.size());

    }

    @NotNull
    private static MinioObjectResponseDto createDto(long userId, String objectName, Long size) {
        boolean isDirectory = objectName.endsWith("/");
        String name = PathUtils.extractName(objectName);
        String path = PathUtils.extractPath(objectName);
        MinioObjectResponseDto.ObjectType objectType = isDirectory ?
                MinioObjectResponseDto.ObjectType.DIRECTORY : MinioObjectResponseDto.ObjectType.FILE;

        return new MinioObjectResponseDto(
                PathUtils.stripUserPrefix(userId,path),
                name,
                isDirectory ? null : size,
                objectType
        );
    }


}
