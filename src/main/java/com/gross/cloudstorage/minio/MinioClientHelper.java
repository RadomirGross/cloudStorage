package com.gross.cloudstorage.minio;

import io.minio.*;
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

    public void createFolder(String bucketName,String folderName) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(folderName)
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

    public Iterable<Result<Item>> getFolder(String bucketName,String folderName)  {
      return  getListObjects(bucketName, folderName, false);
    }

    public Iterable<Result<Item>> getListObjects(String bucketName,String folderName,boolean recursive)  {
        return minioClient.listObjects(
                ListObjectsArgs.builder()
                        .bucket(bucketName)
                        .prefix(folderName)
                        .recursive(recursive)
                        .build()
        );
    }




}

