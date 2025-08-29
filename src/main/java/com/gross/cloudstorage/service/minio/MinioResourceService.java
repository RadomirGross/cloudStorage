package com.gross.cloudstorage.service.minio;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.ResourceAlreadyExistsException;
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
public class MinioResourceService {
    private final MinioClientHelper minioClientHelper;
    private final String bucketName;
    private final Logger logger;
    private final MinioValidationService minioValidationService;

    private MinioResourceService(MinioClientHelper minioClientHelper,
                                @Value("${minio.bucket-name}") String bucketName,
                                MinioValidationService minioValidationService) {
        this.minioClientHelper = minioClientHelper;
        this.bucketName = bucketName;
        this.minioValidationService = minioValidationService;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public MinioDto getResourceInformation(long userId, String path) {
        boolean isDirectory = path.endsWith("/");
        PathUtils.validatePath(path, isDirectory);

        if (!minioValidationService.isResourceExists(path, isDirectory)) {
            throw new ResourceNotFoundException("По пути -" + PathUtils.stripUserPrefix(userId, path) + "не найден" +
                    (isDirectory ? "а директория" : "файл"));
        }

        try {
            return MinioMapper.toDtoFromStat(minioClientHelper.getStatObjectResponse(bucketName, path), userId);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при получении информации о ресурсе {}:", path, e);
            throw new MinioServiceException("Ошибка при получении информации о ресурсе");
        }
    }

    public List<MinioDto> searchResources(long userId, String query) {
        query = query.trim();
        PathUtils.validateSearchRequest(query);
        List<MinioDto> searched = new ArrayList<>();
        Iterable<Result<Item>> results = minioClientHelper.getListObjects(bucketName, PathUtils.addUserPrefix(userId, ""), true);
        try {
            for (Result<Item> result : results) {
                Item item = result.get();
                if (PathUtils.extractName(item.objectName()).toLowerCase()
                        .contains(query.toLowerCase())) {
                    searched.add(MinioMapper.toDto(item, userId));
                }
            }
            return searched;
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при поисковом запросе ", e);
            throw new MinioServiceException("Ошибка при поисковом запросе");
        }
    }

    public MinioDto moveResource(long userId, String from, String to) {
        boolean isDirectory = from.endsWith("/");

        String fullFrom = PathUtils.addUserPrefix(userId, from);
        String fullTo = PathUtils.addUserPrefix(userId, to);

        PathUtils.validatePath(from, isDirectory);
        PathUtils.validatePath(to, isDirectory);

        if (!minioValidationService.isResourceExists(fullFrom,isDirectory)) {
            throw new ResourceNotFoundException("Копируемый ресурс не найден");
        }
        if (minioValidationService.isResourceExists(fullTo,isDirectory)) {
            throw new ResourceAlreadyExistsException("Ресурс по пути " + PathUtils.stripUserPrefix(userId, fullTo) + " уже существует. Операция невозможна.");
        }

        try {
            minioClientHelper.copyResource(bucketName, fullFrom, fullTo, isDirectory);
            deleteResource(userId, from);
            return getResourceInformation(userId, fullTo);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при перемещении или переименовании  ", e);
            throw new MinioServiceException("Ошибка при перемещении или переименовании");
        }
    }

    public void deleteResource(long userId, String path) {
        String fullPath = PathUtils.addUserPrefix(userId, path);
        boolean isDirectory = path.endsWith("/");
        PathUtils.validatePath(fullPath,isDirectory);
        try {
            if (minioValidationService.isResourceExists(path, isDirectory)) {
                throw new ResourceNotFoundException("По пути -" + PathUtils.stripUserPrefix(userId, path) + "не найден" +
                        (isDirectory ? "а директория" : "файл") + "для удаления");
            }

            if (isDirectory) {
                minioClientHelper.deleteDirectory(bucketName, fullPath);
            } else {
                minioClientHelper.deleteFile(bucketName, fullPath);
            }
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при удалении ресурса {}:", path, e);
            throw new MinioServiceException("Ошибка при удалении ресурса");
        }
    }
}
