package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service
public class MinioService {
    private final MinioClientHelper minioClientHelper;
    private final Logger logger;
    String bucketName;

    public MinioService(MinioClientHelper minioClientHelper, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClientHelper = minioClientHelper;
        this.logger = LoggerFactory.getLogger(this.getClass());
        this.bucketName = bucketName;

    }

    @PostConstruct
    public void init() {
        System.out.println("!!!!!!!!!!!!!!!!!!! init bucket");
        createBucket(bucketName);
    }

public List<MinioObjectResponseDto> getUserFolder(long userId, String path) {

    List<MinioObjectResponseDto> objects = getFolder(getFullPathForUser(userId, path));
    System.out.println("api/directory objects:");
    for (MinioObjectResponseDto object : objects) {
        System.out.println(object);
    }
    return objects;
}


    public void createBucket(String bucketName) {
        try {
            if (minioClientHelper.createBucket(bucketName)) {
                logger.info("создался bucket - {}", bucketName);
            } else logger.info("bucket {} уже существует, создание отменено", bucketName);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("ошибка при создании bucket - {}", e.getMessage());
            throw new MinioServiceException("ошибка при создании bucket " + bucketName);
        }
    }

    public MinioObjectResponseDto createFolder(long userId,String folderName) {

        try {
            minioClientHelper.createFolder(bucketName, getFullPathForUser(userId, folderName));

        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при создании папки {}:{}", folderName,e.getMessage());
            throw new MinioServiceException("ошибка при создании папки " + folderName);
        }

        return MinioMapper.toDtoMinioObject(folderName);
    }

    public List<MinioObjectResponseDto> getFolder(String path) {
       return MinioMapper.toListDtoMinioObject(
               minioClientHelper.getFolder(bucketName, path));
    }

    public String getFullPathForUser(long userId,String path) {
        String userFolder = "user-" + userId + "-files/";

        String fullPath= (path == null || path.isEmpty())
                ? userFolder : userFolder+path;

        return fullPath;
    }
}

