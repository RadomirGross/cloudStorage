package com.gross.cloudstorage.service.minio;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.PathValidationException;
import com.gross.cloudstorage.exception.ResourceNotFoundException;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.Result;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
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
public class MinioDirectoryService {
    private final MinioClientHelper minioClientHelper;
    private final MinioValidationService minioValidationService;
    private final String bucketName;
    private final Logger logger;

    private MinioDirectoryService(MinioClientHelper minioClientHelper,
                                 MinioValidationService minioValidationService,
                                 @Value("${minio.bucket-name}")String bucketName) {
        this.minioClientHelper = minioClientHelper;
        this.minioValidationService = minioValidationService;
        this.bucketName = bucketName;
        this.logger = LoggerFactory.getLogger(this.getClass());}

    public List<MinioDto> getUserDirectory(long userId, String path) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        PathUtils.validatePath(fullPath, true);
        if (minioValidationService.isResourceExists(fullPath, true)) {
            return getDirectory(fullPath, userId);
        } else throw new ResourceNotFoundException("Директория по пути " + path + " не найдена");
    }

    public MinioDto createDirectory(long userId, String path, boolean isRootFolder) {
        String fullPath = PathUtils.addUserPrefix(userId, path);

        if (fullPath.split("/").length <= 1 && !isRootFolder) {
            throw new PathValidationException("Недопустимый путь для новой директории");
        }
        PathUtils.validatePath(fullPath, true);
        minioValidationService.validateParentFoldersExist(fullPath, false);

        try {
            minioClientHelper.createFolder(bucketName, fullPath);
            return MinioMapper.toDtoJustForDirectory(fullPath, userId);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при создании папки {}:", path, e);
            throw new MinioServiceException("ошибка при создании папки " + path + e.getMessage());
        }
    }

    private List<MinioDto> getDirectory(String path, long userId) {
        Iterable<Result<Item>> folder = minioClientHelper.getFolder(bucketName, path);
        List<Item> filtered = findAndDeleteDirectoryEmptyFile(folder, path);

        return MinioMapper.toListDto(filtered, userId);
    }

    private List<Item> findAndDeleteDirectoryEmptyFile(Iterable<Result<Item>> folder, String path) {
        List<Item> filtered = new ArrayList<>();
        try {
            for (Result<Item> result : folder) {
                Item item = result.get();
                if (minioValidationService.isDirectoryEmptyFile(item, path)) {
                    continue;
                }
                filtered.add(item);
            }
            return filtered;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при фильтрации папки {}:", path, e);
            throw new MinioServiceException("Ошибка при фильтрации папки");
        }
    }
}





