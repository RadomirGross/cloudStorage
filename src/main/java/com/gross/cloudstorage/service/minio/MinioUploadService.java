package com.gross.cloudstorage.service.minio;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.ResourceAlreadyExistsException;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Service
public class MinioUploadService {
    private final MinioClientHelper minioClientHelper;
    private final String bucketName;
    private final Logger logger;
    private final MinioValidationService minioValidationService;

    public MinioUploadService(MinioClientHelper minioClientHelper,
                              MinioValidationService minioValidationService,
                              @Value("${minio.bucket-name}") String bucketName, MinioValidationService minioValidationService1) {
        this.minioClientHelper = minioClientHelper;
        this.bucketName = bucketName;
        this.minioValidationService = minioValidationService1;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    public MinioDto uploadFile(long userId, String path, MultipartFile object) {
        try {
            String fullPath = PathUtils.addUserPrefix(userId, path);
            PathUtils.validatePath(fullPath, true);
            if (!minioClientHelper.fileExists(bucketName, fullPath + object.getOriginalFilename())) {
                minioClientHelper.uploadFile(bucketName, fullPath, object);
                minioValidationService.validateParentFoldersExist(fullPath + object.getOriginalFilename(), true);
                return MinioMapper.toDto(fullPath + object.getOriginalFilename(), object.getSize(), userId);
            } else throw new ResourceAlreadyExistsException("Файл с таким именем уже существует");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при загрузке ресурса на сервер ", e);
            throw new MinioServiceException("Ошибка при загрузке ресурса на сервер");
        }
    }

    public List<MinioDto> uploadResource(long userId, String path,
                                         List<MultipartFile> objects) {
        List<MinioDto> uploaded = new ArrayList<>();
        for (MultipartFile object : objects) {
            uploaded.add(uploadFile(userId, path, object));
        }
        return uploaded;
    }
}
