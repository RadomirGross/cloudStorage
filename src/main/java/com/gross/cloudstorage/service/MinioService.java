package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import io.minio.errors.MinioException;
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

    public void createFolder(String folderName) {
        try {
            minioClientHelper.createFolder(bucketName, folderName);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при создании папки {}:{}", folderName,e.getMessage());
            throw new MinioServiceException("ошибка при создании папки " + folderName);
        }
    }

    public List<MinioObjectResponseDto> getFolder(String path) {
       return MinioMapper.toListDtoMinioObject(
               minioClientHelper.getFolder(bucketName, path));
    }
}

