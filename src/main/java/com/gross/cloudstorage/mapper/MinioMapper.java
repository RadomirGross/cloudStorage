package com.gross.cloudstorage.mapper;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


public class MinioMapper {

    public static MinioObjectResponseDto toDtoMinioObject(Item item)
            throws MinioServiceException {

        String objectName = item.objectName();
        System.out.println("toDtoMinioObject "+objectName);
        String path = "";
        String name = "";
        int lastSlash = objectName.lastIndexOf("/");
        if (lastSlash != -1) {
            path = objectName.substring(0, lastSlash + 1);
            name = objectName.substring(lastSlash + 1);
        }

        boolean isDirectory = item.isDir();
        System.out.println("isDirectory "+isDirectory);

        return new MinioObjectResponseDto(
                path,
                isDirectory ? name + "/" : name,
                isDirectory ? null : item.size(),
                isDirectory?MinioObjectResponseDto.ObjectType.DIRECTORY:MinioObjectResponseDto.ObjectType.FILE
                );
    }

    public static List<MinioObjectResponseDto> toListDtoMinioObject(Iterable<Result<Item>> minioObjects)
            throws MinioServiceException {
        try{
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
    }catch (Exception e)
        {
            throw new MinioServiceException("Ошибка при маппинге листа объектов из MinIO", e);
        }
    }
}
