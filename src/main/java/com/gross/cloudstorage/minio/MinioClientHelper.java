package com.gross.cloudstorage.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Component
public class MinioClientHelper {
    private final MinioClient minioClient;

    public MinioClientHelper(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public boolean bucketExists(String bucketName) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.bucketExists(
                BucketExistsArgs
                        .builder()
                        .bucket(bucketName)
                        .build());

    }

    public boolean folderExists(String bucketName, String folderPath) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(folderPath)
                            .build()
            );
            return statObjectResponse.size() == 0;
        } catch (ErrorResponseException e) {
            return false;
        }
    }

    public boolean fileExists(String bucketName, String folderPath) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(folderPath)
                            .build()
            );
            return true;
        } catch (ErrorResponseException e) {
            return false;
        }
    }

    public boolean createBucket(String bucketName) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        if (!bucketExists(bucketName)) {
            minioClient.makeBucket(
                    MakeBucketArgs
                            .builder()
                            .bucket(bucketName)
                            .build()
            );
            return true;
        } else return false;
    }

    public void createFolder(String bucketName, String fullPath) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(fullPath)
                        .stream(new ByteArrayInputStream(new byte[0]), 0, -1)
                        .contentType("application/octet-stream")
                        .build()
        );
    }

    public void deleteFolder(String folderName, String bucketName) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        for (Result<Item> result : getListObjects(bucketName, folderName, true)) {
            String objectToDelete = result.get().objectName();
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectToDelete)
                            .build()
            );
        }
    }

    public void deleteFile(String bucketName, String objectToDelete) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectToDelete)
                        .build()
        );
    }


    public Iterable<Result<Item>> getFolder(String bucketName, String path) {
        return getListObjects(bucketName, path, false);
    }

    public Iterable<Result<Item>> getListObjects(String bucketName, String path, boolean recursive) {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(path)
                        .recursive(recursive)
                        .build()
        );
    }


}

