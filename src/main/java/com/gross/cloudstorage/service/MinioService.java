package com.gross.cloudstorage.service;

import com.gross.cloudstorage.dto.MinioObjectResponseDto;
import com.gross.cloudstorage.exception.DirectoryAlreadyExistsException;
import com.gross.cloudstorage.exception.DirectoryPathValidationException;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.MissingParentFolderException;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import io.minio.Result;
import io.minio.errors.*;
import io.minio.messages.Item;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
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
        createBucket(bucketName);
    }

    public List<MinioObjectResponseDto> getUserFolder(long userId, String path) {
        return getFolder(addUserPrefix(userId, path));
    }


    public void createBucket(String bucketName) {
        try {
            if (minioClientHelper.createBucket(bucketName)) {
                logger.info("создался bucket - {}", bucketName);
            } else logger.info("bucket {} уже существует, создание отменено", bucketName);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("ошибка при создании bucket - {}", bucketName, e);
            throw new MinioServiceException("ошибка при создании bucket " + bucketName);
        }
    }

    public MinioObjectResponseDto createFolder(long userId, String path) {
        String fullPath = addUserPrefix(userId, path);
        try {
            if (fullPath.split("/").length <= 1) {
                throw new DirectoryPathValidationException("Недопустимый путь для новой директории");
            }
            validateDirectoryPath(fullPath);
            validateParentFoldersExist(bucketName, fullPath);
            minioClientHelper.createFolder(bucketName, fullPath);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при создании папки {}:", path, e);
            throw new MinioServiceException("ошибка при создании папки " + path + e.getMessage());
        }
        return MinioMapper.toDtoMinioObjectJustForDirectory(fullPath);
    }


    public boolean folderExists(String bucketName, String path) {
        try {
            return minioClientHelper.folderExists(bucketName, path);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при проверке существования папки {}:", path, e);
            throw new MinioServiceException("Ошибка при проверке существования папки " + path + e.getMessage());
        }
    }

    public void validateParentFoldersExist(String bucketName, String path) {
        String[] parts = path.split("/");
        String name = parts[parts.length - 1];
        StringBuilder pathForValidation = new StringBuilder();
        pathForValidation.append(parts[0]).append("/");

        for (int i = 1; i < parts.length - 1; i++) {
            pathForValidation.append(parts[i]).append("/");
            if (!folderExists(bucketName, pathForValidation.toString())) {
                throw new MissingParentFolderException("Не существует родительская директория");
            }
        }

        if (folderExists(bucketName, pathForValidation.append(name).append("/").toString())) {
            throw new DirectoryAlreadyExistsException("Директория по этому пути уже существует");
        }
    }


    public void validateDirectoryPath(String path) {

        if (path == null || path.isEmpty()) {
            throw new DirectoryPathValidationException("Путь директории не может быть пустым");
        }

        if (path.contains("..") || path.contains("//")) {
            throw new DirectoryPathValidationException("Недопустимые символы в пути");
        }

        if (!path.endsWith("/")) {
            throw new DirectoryPathValidationException("Путь директории должен заканчиваться на /");
        }


    }


    public List<MinioObjectResponseDto> getFolder(String path) {
        Iterable<Result<Item>> folder = minioClientHelper.getFolder(bucketName, path);
        List<Item> filtered = findAndDeleteDirectoryEmptyFile(folder, path);
        return MinioMapper.toListDtoMinioObject(filtered);

    }

    public String addUserPrefix(long userId, String path) {
        String userFolder = "user-" + userId + "-files/";
        return userFolder + path;
    }

    public boolean isDirectoryEmptyFile(Item item, String path) {
        return item.objectName().equals(path) && item.size() == 0;

    }

    public List<Item> findAndDeleteDirectoryEmptyFile(Iterable<Result<Item>> folder, String path) {
        List<Item> filtered = new ArrayList<>();
        try {
            for (Result<Item> result : folder) {
                Item item = result.get();
                if (isDirectoryEmptyFile(item, path)) {
                    continue;
                }
                filtered.add(item);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при фильтрации папки {}:", path, e);
            throw new MinioServiceException("Ошибка при фильтрации папки");
        }
        return filtered;

    }

    public boolean deleteResource(String bucketName, String path) {
        return true;
    }
}

