package com.gross.cloudstorage.minio;

import io.minio.*;
import io.minio.errors.ErrorResponseException;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
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


    public StatObjectResponse getStatObjectResponse(String bucketName, String path)
            throws IOException, MinioException, NoSuchAlgorithmException, InvalidKeyException {
        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
        } catch (ErrorResponseException e) {
            return null;
        }
    }

    public long getResourceSize(String bucketName, String path) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        boolean isDirectory = path.endsWith("/");
        long totalSize = 0L;

        if (isDirectory) {
            for (Result<Item> result : getListObjects(bucketName, path, true)) {
                Item item = result.get();
                totalSize += item.size();
            }
            return totalSize;
        } else return getStatObjectResponse(bucketName, path).size();

    }

    public boolean resourceExists(String bucketName, String path, boolean isDirectory) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        StatObjectResponse statObjectResponse = getStatObjectResponse(bucketName, path);
        if (statObjectResponse == null) {
            return false;
        }

        if (!isDirectory) {
            return true;
        }

        return path.endsWith("/") && statObjectResponse.size() == 0;
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

    public void createDirectory(String bucketName, String fullPath) throws IOException, MinioException,
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

    public void deleteDirectory(String bucketName, String path) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        for (Result<Item> result : getListObjects(bucketName, path, true)) {
            String objectToDelete = result.get().objectName();
            deleteFile(bucketName, objectToDelete);
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


    public Iterable<Result<Item>> getDirectory(String bucketName, String path) {
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

    public void uploadFile(String bucketName, String filePath, MultipartFile object) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(filePath + object.getOriginalFilename())
                        .stream(object.getInputStream(), object.getSize(), -1)
                        .contentType(object.getContentType())
                        .build()
        );
    }

    public InputStream getObject(String bucketName, String objectName) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .build()
        );
    }

    public void copyFile(String bucketName, String from, String to) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {

        minioClient.copyObject(
                CopyObjectArgs.builder()
                        .bucket(bucketName)
                        .object(to)
                        .source(
                                CopySource.builder()
                                        .bucket(bucketName)
                                        .object(from)
                                        .build()
                        )
                        .build()
        );
    }

    public void copyDirectory(String bucketName, String from, String to) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        for (Result<Item> listObject : getListObjects(bucketName, from, true)) {
            String objectToCopy = listObject.get().objectName();
            copyFile(bucketName, objectToCopy, to + objectToCopy.substring(from.length()));
        }
    }

    public void copyResource(String bucketName, String from, String to, boolean isDirectory) throws IOException, MinioException,
            NoSuchAlgorithmException, InvalidKeyException {
        if (isDirectory) {
            copyDirectory(bucketName, from, to);
        } else copyFile(bucketName, from, to);
    }


}

