package com.gross.cloudstorage.service.minio;

import com.gross.cloudstorage.dto.MinioDto;
import com.gross.cloudstorage.exception.MinioServiceException;
import com.gross.cloudstorage.exception.ResourceAlreadyExistsException;
import com.gross.cloudstorage.exception.StorageQuotaExceededException;
import com.gross.cloudstorage.mapper.MinioMapper;
import com.gross.cloudstorage.minio.MinioClientHelper;
import com.gross.cloudstorage.service.StorageProtectionService;
import com.gross.cloudstorage.utils.PathUtils;
import io.minio.errors.MinioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.unit.DataSize;
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
    private final StorageProtectionService storageProtectionService;
    private static final int DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE = 2;

    @Value("${spring.servlet.multipart.max-file-size}")
    private DataSize maxFileSize;

    private MinioUploadService(MinioClientHelper minioClientHelper,
                               @Value("${minio.bucket-name}") String bucketName,
                               MinioValidationService minioValidationService,
                               StorageProtectionService storageProtectionService) {
        this.minioClientHelper = minioClientHelper;
        this.bucketName = bucketName;
        this.minioValidationService = minioValidationService;
        this.storageProtectionService = storageProtectionService;
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

    private MinioDto uploadFile(long userId, String path, MultipartFile object) {
        try {
            if (object.getSize() > maxFileSize.toBytes()) {
                throw new StorageQuotaExceededException("Превышен размер одного файла в 5 GB, вы пытались загрузить - " + object.getSize() / 1024 / 1024 / 1024 + " GB");
            }

            String fileName = object.getOriginalFilename();
            String fullPath = PathUtils.addUserPrefix(userId, path);
            PathUtils.validatePath(fullPath, true);

            if (!minioClientHelper.resourceExists(bucketName, fullPath + fileName, false)) {
                minioValidationService.validateParentFoldersExist(fullPath + fileName, true);
                minioValidationService.validateNoNameConflicts(bucketName, fullPath, fileName);
                minioClientHelper.uploadFile(bucketName, fullPath, object);
                logger.info("Успешно загружен файл: {}, размером: {} байт", fullPath + fileName, object.getSize());
                return MinioMapper.toDto(fullPath + fileName, object.getSize(), userId);
            } else throw new ResourceAlreadyExistsException("Файл с таким именем уже существует");
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | MinioException e) {
            logger.error("Ошибка при загрузке ресурса на сервер ", e);
            throw new MinioServiceException("Ошибка при загрузке ресурса на сервер");
        }
    }

    public List<MinioDto> uploadResource(long userId, String path,
                                         List<MultipartFile> objects, long contentLength) {

        try {
            List<MinioDto> uploaded = new ArrayList<>();
            for (MultipartFile object : objects) {
                uploaded.add(uploadFile(userId, path, object));
            }
            return uploaded;
        } finally {
            storageProtectionService.removeReservedSpace(contentLength * DISK_USAGE_MULTIPLIER_TMP_AND_STORAGE);
        }
    }


}
