package com.gross.cloudstorage.service.minio;

import com.gross.cloudstorage.exception.MinioServiceException;
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

@Service
public class MinioBucketService {
    private final MinioClientHelper minioClientHelper;
    private final String bucketName;
    private final Logger logger;

    private MinioBucketService(MinioClientHelper minioClientHelper, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClientHelper = minioClientHelper;
        this.bucketName = bucketName;
        this.logger = LoggerFactory.getLogger(this.getClass());}

    private void createBucket(String bucketName) {
        try {
            if (minioClientHelper.createBucket(bucketName)) {
                logger.info("создался bucket - {}", bucketName);
            } else logger.info("bucket {} уже существует, создание отменено", bucketName);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("ошибка при создании bucket - {}", bucketName, e);
            throw new MinioServiceException("ошибка при создании bucket " + bucketName);
        }
    }

    @PostConstruct
    private void init() {
        createBucket(bucketName);
    }

}
