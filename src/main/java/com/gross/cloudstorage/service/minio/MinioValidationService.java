package com.gross.cloudstorage.service.minio;

import com.gross.cloudstorage.exception.DirectoryAlreadyExistsException;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.MissingParentFolderException;
import com.gross.cloudstorage.minio.MinioClientHelper;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioValidationService {
    private final MinioClientHelper minioClientHelper;
    private final String bucketName;
    private final Logger logger;

    public MinioValidationService(MinioClientHelper minioClientHelper, @Value("${minio.bucket-name}") String bucketName) {
        this.minioClientHelper = minioClientHelper;
        this.bucketName = bucketName;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public boolean isResourceExists(String path, boolean isDirectory) {
        try {
            return minioClientHelper.resourceExists(bucketName, path, isDirectory);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при проверке существования ресурса {}:", path.substring(path.indexOf('/') + 1), e);
            throw new MinioServiceException("Ошибка при проверке существования ресурса ");
        }
    }

    public void validateParentFoldersExist(String path, boolean createNonExistentFolders) {
        String[] parts = path.split("/");
        String name = parts[parts.length - 1];
        StringBuilder pathForValidation = new StringBuilder();
        pathForValidation.append(parts[0]).append("/");

        for (int i = 1; i < parts.length - 1; i++) {
            pathForValidation.append(parts[i]).append("/");
            if (!isResourceExists(pathForValidation.toString(), true)) {
                if (createNonExistentFolders) {
                    try {
                        minioClientHelper.createFolder(bucketName, pathForValidation.toString());
                    } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
                        throw new MinioServiceException("Ошибка при создании директории. ");
                    }
                } else throw new MissingParentFolderException("Не существует родительская директория");
            }
        }
        if (isResourceExists(pathForValidation.append(name).append("/").toString(), true)) {
            throw new DirectoryAlreadyExistsException("Директория по этому пути уже существует");
        }
    }

    public boolean isDirectoryEmptyFile(Item item, String path) {
        return item.objectName().equals(path) && item.size() == 0 && item.objectName().endsWith("/");

    }
}



