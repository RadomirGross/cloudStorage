package com.gross.cloudstorage.mapper;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;


public class MinioMapper {


    public static MinioDto toDto(Item item, long userId) {
        String objectName = item.objectName();
        return createDto(userId, objectName, item.size());
    }

    public static MinioDto toDto(String objectName, Long size, long userId) {
        return createDto(userId, objectName, size);
    }


    public static List<MinioDto> toListDto(List<Item> minioItems, long userId) {
        List<MinioDto> list = new ArrayList<>();
        for (Item item : minioItems) {
            list.add(toDto(item, userId));
        }
        return list;
    }


    public static MinioDto toDtoJustForDirectory(String objectName, long userId) {
        if (!objectName.endsWith("/")) {
            throw new MinioServiceException("Путь директории должен заканчиваться на /");
        }
        return createDto(userId, objectName, null);
    }

    public static MinioDto toDtoFromStat(StatObjectResponse statObjectResponse, long userId) {
        String objectName = statObjectResponse.object();
        return createDto(userId, objectName, statObjectResponse.size());

    }

    @NotNull
    private static MinioDto createDto(long userId, String objectName, Long size) {
        boolean isDirectory = objectName.endsWith("/");
        String name = PathUtils.extractName(objectName);
        String path = PathUtils.extractPath(objectName);
        MinioDto.ObjectType objectType = isDirectory ?
                MinioDto.ObjectType.DIRECTORY : MinioDto.ObjectType.FILE;

        return new MinioDto(
                PathUtils.stripUserPrefix(userId,path),
                name,
                isDirectory ? null : size,
                objectType
        );
    }


}
