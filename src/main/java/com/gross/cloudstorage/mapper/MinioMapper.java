package com.gross.cloudstorage.mapper;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import io.minio.Result;
import io.minio.messages.Item;
import java.util.ArrayList;
import java.util.List;


public class MinioMapper {


    public static MinioObjectResponseDto toDtoMinioObject(Item item)
            throws MinioServiceException {

        String objectName = item.objectName();
        System.out.println("objectName:"+objectName);
        boolean isDirectory = objectName.endsWith("/");
        String[] parts = objectName.split("/");
        for (String part : parts) {
            System.out.println("part:"+part);
        }

        String name;
        if (isDirectory) {
            if(parts.length > 2)
            {   name = parts[parts.length - 2]+"/";}
            else {name = parts[parts.length - 1]+"/";}
        } else {
            name = parts[0];
        }

        // Путь (все части, кроме имени):
        System.out.println("nameLength:"+name.length());
        String path = objectName.substring(0, objectName.length() - name.length());

        return new MinioObjectResponseDto(
                path,
                name,
                item.size(),
                isDirectory ? MinioObjectResponseDto.ObjectType.DIRECTORY : MinioObjectResponseDto.ObjectType.FILE
        );
    }


    public static List<MinioObjectResponseDto> toListDtoMinioObject(Iterable<Result<Item>> minioObjects)
            throws MinioServiceException {
        try {
            List<MinioObjectResponseDto> list = new ArrayList<>();
            for (Result<Item> result : minioObjects) {
                try {
                    Item item = result.get();
                    list.add(toDtoMinioObject(item));
                } catch (Exception e) {
                    throw new MinioServiceException("Ошибка при получении объекта из MinIO: ", e);
                }
            }
            return list;
        } catch (Exception e) {
            throw new MinioServiceException("Ошибка при маппинге листа объектов из MinIO", e);
        }
    }

    public static MinioObjectResponseDto toDtoMinioObject(String objectName)
            throws MinioServiceException {
        boolean isDirectory = objectName.endsWith("/");
        String[] parts = objectName.split("/");

        // Имя:
        String name;
        if (isDirectory) {
            if(parts.length > 2)
            {   name = parts[parts.length - 2]+"/";}
            else {name = parts[parts.length - 1]+"/";}
        } else {
            name = parts[0];
        }
        System.out.println("toDtoMinioObject(String objectName)");
        for (String part : parts) {
            System.out.println(part);
        }
        // Путь (все части, кроме имени):
        String path = objectName.substring(0, objectName.length() - name.length());

        return new MinioObjectResponseDto(
                path,
                name,
                0L,
                isDirectory ? MinioObjectResponseDto.ObjectType.DIRECTORY : MinioObjectResponseDto.ObjectType.FILE
        );
    }
}
